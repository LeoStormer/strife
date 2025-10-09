import { Link } from "react-router-dom";

function RegistrationForm({ handleSubmit }) {
  return (
    <div className="form-container">
      <form className="registration-form" onSubmit={handleSubmit}>
        <div className="form-group">
          <label>Email</label>
          <br />
          <input type="email" name="email" required />
        </div>
        <div className="form-group">
          <label>Username</label>
          <br />
          <input type="text" name="username" required />
        </div>
        <div className="form-group">
          <label>Password</label>
          <br />
          <input type="password" name="password" required />
        </div>
        <div className="form-group">
          <button type="submit">Create Account</button>
          <br />
          <Link to="/login">Already have an account? Log in</Link>
        </div>
      </form>
    </div>
  );
}

export default RegistrationForm;
