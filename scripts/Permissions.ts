export const PermissionType = {
  VIEW_CHANNELS: 1n << 0n,
  MANAGE_CHANNELS: 1n << 1n,
  MANAGE_ROLES: 1n << 2n,
  // CREATE_EXPRESSIONS: 1n << 3n,
  // MANAGE_EXPRESSIONS: 1n << 4n,
  // VIEW_AUDIT_LOG: 1n << 5n,
  // MANAGE_WEBHOOKS: 1n << 6n,
  MANAGE_SERVER: 1n << 7n,
  CREATE_INVITE: 1n << 8n,
  CHANGE_NICKNAME: 1n << 9n,
  MANAGE_NICKNAMES: 1n << 10n,
  KICK_MEMBERS: 1n << 11n,
  BAN_MEMBERS: 1n << 12n,
  TIMEOUT_MEMBERS: 1n << 13n,
  SEND_MESSAGES: 1n << 14n,
  MANAGE_MESSAGES: 1n << 15n,
  PIN_MESSAGES: 1n << 16n,
  // SEND_MESSAGES_IN_THREADS: 1n << 17n,
  // CREATE_PUBLIC_THREADS: 1n << 18n,
  // CREATE_PRIVATE_THREADS: 1n << 19n,
  EMBED_LINKS: 1n << 20n,
  // ATTACH_FILES: 1n << 21n,
  ADD_REACTIONS: 1n << 22n,
  USE_EXTERNAL_EMOJIS: 1n << 23n,
  USE_EXTERNAL_STICKERS: 1n << 24n,
  MENTIONS: 1n << 25n,
  // MANAGE_THREADS: 1n << 26n,
  READ_MESSAGE_HISTORY: 1n << 27n,
  // SEND_TEXT_TO_SPEECH: 1n << 28n,
  // SEND_VOICE_MESSAGES: 1n << 29n,
  // CREATE_POLLS: 1n << 30n,
  // CONNECT_VOICE: 1n << 31n,
  // SPEAK_VOICE: 1n << 32n,
  // SHARE_VIDEO: 1n << 33n,
  // MUTE_MEMBERS: 1n << 34n,
  // DEAFEN_MEMBERS: 1n << 35n,
  // MOVE_MEMBERS: 1n << 36n,
  // USE_SOUNDBOARD: 1n << 37n,
  // USE_EXTERNAL_SOUNDS: 1n << 38n,
  // USE_VOICE_ACTIVITY: 1n << 39n,
  // PRIORITY_SPEAKER: 1n << 40n,
  // SET_VOICE_CHANNEL_STATUS: 1n << 41n,
  // USE_APPLICATION_COMMANDS: 1n << 42n,
  // USE_ACTIVITIES: 1n << 43n,
  // USE_EXTERNAL_APPS: 1n << 44n,
  // CREATE_EVENTS: 1n << 45n,
  // MANAGE_EVENTS: 1n << 46n,
  ADMINISTRATOR: 1n << 63n,
} as const;

export class Permissions {
  public static readonly NONE = 0n;
  // 64-bit unsigned integer with all bits set
  public static readonly ALL = (1n << 64n) - 1n;

  public static grantPermission(
    currentPermissions: bigint,
    ...permissionTypes: bigint[]
  ): bigint {
    return permissionTypes.reduce(
      (acc, type) => acc | type,
      currentPermissions,
    );
  }

  public static getPermissions(...permissionTypes: bigint[]): bigint {
    return this.grantPermission(this.NONE, ...permissionTypes);
  }

  public static combinePermissions(perm1: bigint, perm2: bigint): bigint {
    return perm1 | perm2;
  }
}
