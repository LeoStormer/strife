import { MongoClient, ObjectId } from "mongodb";
import { faker } from "@faker-js/faker";
import bcrypt from "bcryptjs";
import { Permissions, PermissionType } from "./Permissions.ts";

const database = process.env.MONGO_DATABASE || "strife";
const uri = process.env.MONGO_URI;
if (!uri) {
  throw new Error("MONGO_URI is not defined");
}

const client = new MongoClient(uri);

type User = ReturnType<typeof createUser>;
type Server = ReturnType<typeof createServer>;
type Role = ReturnType<typeof createRole>;

const createUser = (
  name = faker.internet.username(),
  email = faker.internet.email().toLowerCase(),
  password = faker.internet.password(),
) => {
  const hashedPassword = bcrypt.hashSync(password, 12);

  return {
    _id: new ObjectId(),
    username: name,
    email,
    password: hashedPassword,
    profilePic: null,
    friends: [] as ObjectId[],
    blockedUsers: [] as ObjectId[],
    createdDate: new Date(),
    _class: "com.leostormer.strife.user.User",
  };
};

const createRole = ({
  name,
  priority = 0,
  permissions = Permissions.NONE,
}: {
  name: string;
  priority?: number;
  permissions?: bigint;
}) => {
  return {
    _id: new ObjectId(),
    name,
    permissions,
    priority,
  };
};

const createServer = (name: string, owner: ObjectId) => {
  return {
    _id: new ObjectId(),
    name,
    desciption: faker.company.catchPhrase(),
    icon: null,
    owner,
    roles: {} as Record<string, Role>,
    createdAt: new Date(),
    _class: "com.leostormer.strife.server.Server",
  };
};

const createMember = ({
  user,
  server,
  roles = [],
  nickname = user.username,
  isOwner = false,
  isBanned = false,
  banreason,
}: {
  user: User;
  server: Server;
  roles?: Role[];
  nickname?: string;
  isOwner?: boolean;
  isBanned?: boolean;
  banreason?: string;
}) => {
  return {
    _id: new ObjectId(),
    user: user._id,
    server: server._id,
    nickname,
    isOwner,
    isBanned,
    banreason,
    rolePriority: roles.reduce((acc, role) => Math.max(acc, role.priority), 0),
    roleIds: roles.map((role) => role._id),
    permissions: roles.reduce((acc, role) => acc | role.permissions, 0n),
    joinedAt: new Date(),
    _class: "com.leostormer.strife.member.Member",
  };
};

const createServerChannel = ({
  name,
  category,
  description,
  server,
  isPublic = true,
  rolePermissions = {},
  userPermissions = {},
}: {
  name: string;
  category: string;
  server: ObjectId;
  description?: string;
  isPublic?: boolean;
  rolePermissions?: Record<string, bigint>;
  userPermissions?: Record<string, bigint>;
}) => {
  return {
    _id: new ObjectId(),
    name,
    category,
    description,
    isPublic,
    server,
    rolePermissions,
    userPermissions,
    createdAt: new Date(),
    _class: "com.leostormer.strife.server.server_channel.ServerChannel",
  };
};

const createConversation = ({
  userPresenceMap,
  locked = false,
}: {
  userPresenceMap: Record<string, boolean>;
  locked?: boolean;
}) => {
  return {
    _id: new ObjectId(),
    locked,
    numUsers: Object.keys(userPresenceMap).length,
    userPresenceMap,
    createdAt: new Date(),
    _class: "com.leostormer.strife.conversation.Conversation",
  };
};

const createFriendRequest = ({
  sender,
  receiver,
  accepted = false,
}: {
  sender: ObjectId;
  receiver: ObjectId;
  accepted?: boolean;
}) => {
  return {
    _id: new ObjectId(),
    sender,
    receiver,
    accepted,
    _class: "com.leostormer.strife.user.friends.FriendRequest",
  };
};

const createMessage = ({
  content = faker.lorem.sentence(),
  sender,
  channel,
}: {
  content?: string;
  sender: ObjectId;
  channel: ObjectId;
}) => {
  return {
    _id: new ObjectId(),
    content,
    sender,
    channel,
    timestamp: new Date(),
    _class: "com.leostormer.strife.message.Message",
  };
};

async function seedServer(
  testUser: User,
  users: User[],
  db: ReturnType<MongoClient["db"]>,
) {
  const testServer = createServer("Test Server", testUser._id);
  const ownerRole = createRole({
    name: "Owner",
    priority: 2147483647,
    permissions: Permissions.ALL,
  });
  const adminRole = createRole({
    name: "Admin",
    priority: 1000,
    permissions: Permissions.getPermissions(
      PermissionType.VIEW_CHANNELS,
      PermissionType.MANAGE_CHANNELS,
      PermissionType.MANAGE_ROLES,
      PermissionType.MANAGE_SERVER,
      PermissionType.CREATE_INVITE,
      PermissionType.KICK_MEMBERS,
      PermissionType.BAN_MEMBERS,
      PermissionType.TIMEOUT_MEMBERS,
      PermissionType.SEND_MESSAGES,
      PermissionType.MANAGE_MESSAGES,
      PermissionType.PIN_MESSAGES,
      PermissionType.EMBED_LINKS,
      PermissionType.ADD_REACTIONS,
      PermissionType.USE_EXTERNAL_EMOJIS,
      PermissionType.USE_EXTERNAL_STICKERS,
      PermissionType.MENTIONS,
      PermissionType.READ_MESSAGE_HISTORY,
    ),
  });
  const defaultRole = createRole({
    name: "Member",
    priority: 0,
    permissions: Permissions.getPermissions(
      PermissionType.VIEW_CHANNELS,
      PermissionType.SEND_MESSAGES,
      PermissionType.CHANGE_NICKNAME,
    ),
  });
  testServer.roles = {
    [ownerRole._id.toHexString()]: ownerRole,
    [adminRole._id.toHexString()]: adminRole,
    [defaultRole._id.toHexString()]: defaultRole,
  };

  await db.collection("servers").insertOne(testServer);

  const ownerMember = createMember({
    user: testUser,
    server: testServer,
    roles: [ownerRole],
    isOwner: true,
  });
  const adminMember = createMember({
    user: users[0],
    server: testServer,
    roles: [adminRole],
  });
  const defaultMember = createMember({
    user: users[1],
    server: testServer,
    roles: [defaultRole],
  });

  // Seed Channels
  const generalChannel = createServerChannel({
    name: "general",
    category: "General",
    description: "The general discussion channel",
    server: testServer._id,
  });
  const loungeChannel = createServerChannel({
    name: "staff-discussion",
    category: "staff",
    description: "The staff discussion channel",
    server: testServer._id,
    isPublic: false,
    rolePermissions: {
      [adminRole._id.toHexString()]: Permissions.getPermissions(
        PermissionType.VIEW_CHANNELS,
        PermissionType.SEND_MESSAGES,
      ),
    },
  });

  const messages = Array.from({ length: 10 }).map(() => {
    const sender = [testUser._id, users[0]._id, users[1]._id][
      Math.floor(Math.random() * 3)
    ];
    return createMessage({ sender, channel: generalChannel._id });
  });
  const staffMessages = Array.from({ length: 5 }).map(() => {
    const sender = [testUser._id, users[0]._id][Math.floor(Math.random() * 2)];
    return createMessage({ sender, channel: loungeChannel._id });
  });

  await Promise.all([
    db
      .collection("members")
      .insertMany([ownerMember, adminMember, defaultMember], {
        ordered: false,
      }),
    db
      .collection("channels")
      .insertMany([generalChannel, loungeChannel], { ordered: false }),
    db
      .collection("messages")
      .insertMany([...messages, ...staffMessages], { ordered: false }),
  ]);
}

const seedConversations = async (
  testUser: User,
  users: User[],
  db: ReturnType<MongoClient["db"]>,
) => {
  const conversations = [
    createConversation({
      userPresenceMap: {
        [testUser._id.toHexString()]: true,
        [users[0]._id.toHexString()]: true,
      },
    }),
    createConversation({
      userPresenceMap: {
        [testUser._id.toHexString()]: true,
        [users[1]._id.toHexString()]: false,
      },
    }),
    createConversation({
      userPresenceMap: {
        [testUser._id.toHexString()]: false,
        [users[2]._id.toHexString()]: true,
      },
    }),
    createConversation({
      locked: true,
      userPresenceMap: {
        [testUser._id.toHexString()]: true,
        [users[3]._id.toHexString()]: false,
      },
    }),
    createConversation({
      userPresenceMap: {
        [testUser._id.toHexString()]: true,
        [users[0]._id.toHexString()]: true,
        [users[1]._id.toHexString()]: true,
      },
    }),
  ];

  // seed some messages in the conversations
  const conversationMessages = [];
  for (const conversation of conversations) {
    const userIds = Object.keys(conversation.userPresenceMap);
    for (let i = 0; i < 5; i++) {
      const sender = userIds[Math.floor(Math.random() * userIds.length)];
      conversationMessages.push(
        createMessage({
          sender: new ObjectId(sender),
          channel: conversation._id,
        }),
      );
    }
  }

  await Promise.all([
    db.collection("channels").insertMany(conversations, { ordered: false }),
    db
      .collection("messages")
      .insertMany(conversationMessages, { ordered: false }),
  ]);
};

async function seed() {
  try {
    await client.connect();
    const db = client.db(database);

    console.log("🧹 Dropping database for fresh start...");
    await db.dropDatabase();
    console.log("✅ Database cleared!");

    // Seed users
    const testUser = createUser("testuser", "testuser@example.com", "password");
    const users = Array.from({ length: 4 }).map(() => createUser());
    testUser.friends.push(users[0]._id); // make the first user a friend for testing
    users[0].friends.push(testUser._id); // make the first user a friend for testing
    testUser.blockedUsers.push(users[3]._id); // block the last user for testing
    users.push(testUser);

    // Seed friend requests - make testUser friends with first user,
    // send a pending request to the 2nd user,
    // and receive a pending request from the 3rd user
    const friendRequests = [
      createFriendRequest({
        sender: testUser._id,
        receiver: users[0]._id,
        accepted: true,
      }),
      createFriendRequest({
        sender: testUser._id,
        receiver: users[1]._id,
        accepted: false,
      }),
      createFriendRequest({
        sender: users[2]._id,
        receiver: testUser._id,
        accepted: false,
      }),
    ];

    // make all other users friends with each other excluding testUser
    for (let i = 0; i < users.length - 2; i++) {
      const userA = users[i];
      for (let j = i + 1; j < users.length - 1; j++) {
        const userB = users[j];
        userA.friends.push(userB._id);
        userB.friends.push(userA._id);
        friendRequests.push(
          createFriendRequest({
            sender: userA._id,
            receiver: userB._id,
            accepted: true,
          }),
        );
      }
    }

    await db.collection("users").insertMany(users, { ordered: false });
    console.log(`✅ Seeded ${users.length} users with TypeScript!`);

    // Seed server
    await Promise.all([
      db
        .collection("friend_requests")
        .insertMany(friendRequests, { ordered: false }),
      seedServer(testUser, users, db),
      seedConversations(testUser, users, db),
    ]);

    console.log(
      "✅ Seeding complete! Login with testuser@example.com / password",
    );
  } finally {
    await client.close();
  }
}

seed().catch(console.error);
