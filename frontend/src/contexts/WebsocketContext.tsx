import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  type PropsWithChildren,
} from "react";
import {
  Client,
  ReconnectionTimeMode,
  type Message,
  type StompSubscription,
} from "@stomp/stompjs";
import SockJS from "sockjs-client";

type WebsocketContextType = {
  subscribe: (
    destination: string,
    callback: (message: Message) => void,
  ) => void;
  unsubscribe: (destination: string) => void;
  send: (destination: string, body: Record<string, any>) => void;
};

const WebsocketContext = createContext<WebsocketContextType>({
  subscribe: () => {},
  unsubscribe: () => {},
  send: () => {},
});

const WEBSOCKET_URL = process.env.NEXT_PUBLIC_WEBSOCKET_URL;

if (!WEBSOCKET_URL) {
  throw new Error("WEBSOCKET_URL is not defined.");
}

function WebsocketContextProvider({ children }: PropsWithChildren) {
  const clientRef = useRef<Client | null>(null);
  const subscriptionsRef = useRef<Record<string, StompSubscription>>({});
  const desiredSubscriptionsRef = useRef<
    Record<string, (message: Message) => void>
  >({});

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS(WEBSOCKET_URL!),
      reconnectTimeMode: ReconnectionTimeMode.EXPONENTIAL,
      reconnectDelay: 1000,
      onConnect: () => {
        console.log("Websocket Connected");
        subscriptionsRef.current = {};

        for (const [destination, callback] of Object.entries(
          desiredSubscriptionsRef.current,
        )) {
          const subscription = client.subscribe(destination, callback);
          subscriptionsRef.current[destination] = subscription;
        }
      },
      onStompError: (error) => {
        const errorMessage = error.headers["message"] || "Unknown Error";
        console.log(`Websocket Error: ${errorMessage}`);
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
      clientRef.current = null;
    };
  }, []);

  const subscribe = useCallback(
    (destination: string, callback: (message: Message) => void) => {
      desiredSubscriptionsRef.current[destination] = callback;

      const client = clientRef.current;
      if (!client?.connected) {
        return;
      }

      if (subscriptionsRef.current[destination]) {
        subscriptionsRef.current[destination].unsubscribe();
      }

      const subscription = client.subscribe(destination, callback);
      subscriptionsRef.current[destination] = subscription;
    },
    [],
  );

  const unsubscribe = useCallback((destination: string) => {
    const subscription = subscriptionsRef.current[destination];
    if (subscription) {
      subscription.unsubscribe();
      delete subscriptionsRef.current[destination];
      delete desiredSubscriptionsRef.current[destination];
    }
  }, []);

  const send = useCallback((destination: string, body: Record<string, any>) => {
    const client = clientRef.current;
    if (!(client && client.connected)) {
      return;
    }

    client.publish({ destination, body: JSON.stringify(body) });
  }, []);

  const value = useMemo<WebsocketContextType>(
    () => ({ subscribe, unsubscribe, send }),
    [subscribe, unsubscribe, send],
  );

  return <WebsocketContext value={value}>{children}</WebsocketContext>;
}

export const useWebsocketContext = () => {
  const context = useContext(WebsocketContext);
  if (!context) {
    throw new Error(
      "useWebsocketContext must be used within a WebsocketContextProvider",
    );
  }

  return context;
};

export default WebsocketContextProvider;
