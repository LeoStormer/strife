import { FormEventHandler } from "react";
import { Link } from "react-router-dom";

function RegistrationForm({ handleSubmit }: { handleSubmit: FormEventHandler<HTMLFormElement>}) {
  return (
    <div className="form-container">
      <form className="registration-form" onSubmit={handleSubmit}>
        <div className="wrapper">
          <div className="form-group">
            <label htmlFor="email">Email</label>
            <br />
            <input type="email" name="email" id="email" required />
          </div>
          <div className="form-group">
            <label htmlFor="username">Username</label>
            <br />
            <input type="text" name="username" id="username" required />
          </div>
          <div className="form-group">
            <label htmlFor="password">Password</label>
            <br />
            <input type="password" name="password" id="password" required />
          </div>
          <div className="form-group">
            <button type="submit">Create Account</button>
            <br />
            <Link to="/login">Already have an account? Log in</Link>
          </div>
        </div>
      </form>
    </div>
  );
}

export default RegistrationForm;
