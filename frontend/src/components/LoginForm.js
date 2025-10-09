import { Link } from "react-router-dom";

function LoginForm({ handleSubmit }) {
  return (
    <div className="form-container">
      <form className="login-form" onSubmit={handleSubmit}>
        <div className="form-group">
          <label>Email</label>
          <br />
          <input type="email" name="email" required />
        </div>
        <div className="form-group">
          <label>Password</label>
          <br />
          <input type="password" name="password" required />
        </div>
        <div className="form-group">
          <button type="submit">Login</button>
          <br />
          <label>
            Need an Account? <Link to="/register">Register</Link>
          </label>
        </div>
      </form>
    </div>
  );
}

export default LoginForm;
