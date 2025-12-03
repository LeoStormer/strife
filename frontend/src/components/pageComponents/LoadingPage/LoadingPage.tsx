import styles from "./LoadingPage.module.css";

function LoadingPage() {
  return (
    <div className={styles.container}>
      <div className={styles.loader}>LOADING...</div>
    </div>
  );
}

export default LoadingPage;
