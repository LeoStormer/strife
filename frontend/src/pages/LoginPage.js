import LoginForm from "../components/LoginForm";

function LoginPage() {
  const handleSubmit = (e) => {
    e.preventDefault();
    const formdata = new FormData(e.currentTarget);
    const payload = Object.fromEntries(formdata.entries());
    // Given the formdata, send a login request to the backend.
    // On success navigate to the user's friend page.
    // On failure show an error message.
  };

  return (
    <div>
      <LoginForm handleSubmit={handleSubmit} />
    </div>
  );
}

export default LoginPage;
