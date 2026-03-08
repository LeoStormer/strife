import Skeleton from "react-loading-skeleton";
import ProfilePicture from "../../../../components/ProfilePicture";
import styles from "./ProfileDisplayGrid.module.css";
import { useUserCacheContext } from "../../../../contexts/UserCacheContext";
import { Suspense, use, useMemo } from "react";
import { ErrorBoundary } from "react-error-boundary";
import Icon from "../../../../components/Icon";

const ProfileGridContent = ({ userIds }: { userIds: string[] }) => {
  const { getUser } = useUserCacheContext();

  const usersPromise = useMemo(async () => {
    const targets = userIds.slice(0, 4);
    return Promise.allSettled(targets.map((id) => getUser(id))).then(
      (results) =>
        results.map((res, index) => {
          const id = targets[index];
          return res.status === "fulfilled"
            ? ({ id, user: res.value, isError: false } as const)
            : ({ id, user: null, isError: true } as const);
        }),
    );
  }, [userIds, getUser]);

  const results = use(usersPromise);

  return (
    <div className={styles.profileDisplayGrid}>
      {results.map((result) => {
        if (result.isError) {
          return (
            <Icon
              name='error-circle'
              key={result.id}
              className={styles.error}
            />
          );
        }

        const user = result.user;
        return (
          <ProfilePicture
            key={result.id}
            profilePic={user.profilePic}
            alt={user.username}
          />
        );
      })}
    </div>
  );
};

const ProfileDisplayGrid = ({ userIds }: { userIds: string[] }) => {
  return (
    <ErrorBoundary
      fallback={<Icon name='error-circle' className={styles.error} />}
    >
      <Suspense
        fallback={
          <Skeleton
            containerClassName={styles.profileDisplaySkeleton as string}
            circle
          />
        }
      >
        <ProfileGridContent userIds={userIds} />
      </Suspense>
    </ErrorBoundary>
  );
};

export default ProfileDisplayGrid;
