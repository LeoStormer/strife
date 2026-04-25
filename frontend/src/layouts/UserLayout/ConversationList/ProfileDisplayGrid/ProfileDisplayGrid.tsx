import Skeleton from "react-loading-skeleton";
import ProfilePicture from "../../../../components/ProfilePicture";
import styles from "./ProfileDisplayGrid.module.css";
import { Suspense } from "react";
import { ErrorBoundary } from "react-error-boundary";
import Icon from "../../../../components/Icon";
import { useUserList } from "../../../../contexts/UserContext";

const ProfileGridContent = ({ userIds }: { userIds: string[] }) => {
  const truncatedUserIds = userIds.slice(0, 4);
  const results = useUserList(truncatedUserIds);

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
