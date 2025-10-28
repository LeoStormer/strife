import { createContext, ReactNode, useState } from "react";

export type User = {
  id: string;
  username: string;
  profilePic: string;
  createdDate: Date;
};

interface UserContextType {
  user: User | null;
  login: (userData: User) => void;
  logout: () => void;
}

export const UserContext = createContext<UserContextType>({
  user: null,
  login: (userData: User) => {},
  logout: () => {},
});

export const UserContextProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<User | null>(null);
  const login = (userData: User) => setUser(userData);
  const logout = () => setUser(null);

  return <UserContext value={{ user, login, logout }}>{children}</UserContext>;
};
