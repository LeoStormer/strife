import { type FormEventHandler } from "react";
import { Link } from "react-router-dom";
import formStyles from "../../../../styles/Form.module.css";
import { REGISTRATION_PAGE_PATH } from "../../../../constants";

type LoginFormProps = {
  handleSubmit: FormEventHandler<HTMLFormElement>;
};

function LoginForm({ handleSubmit }: LoginFormProps) {
  return (
    <div className={formStyles.container}>
      <form className={formStyles.form} onSubmit={handleSubmit}>
        <header className={formStyles.header}>
          <h2>Welcome back!</h2>
          <p>We're so excited to see you again!</p>
        </header>
        <div>
          <label className={formStyles.inputLabel} htmlFor='email'>
            Email
          </label>
          <input
            className={formStyles.input}
            type='email'
            name='email'
            id='email'
            required
          />
        </div>
        <div>
          <label className={formStyles.inputLabel} htmlFor='password'>
            Password
          </label>
          <input
            className={formStyles.input}
            type='password'
            name='password'
            id='password'
            required
          />
          <label className={`${formStyles.label} ${formStyles.link}`}>
            Forgot your password?
          </label>
        </div>
        <div>
          <button className={formStyles.button} type='submit'>
            Log In
          </button>
          <label className={formStyles.label}>
            Need an Account?{" "}
            <Link className={formStyles.link} to={REGISTRATION_PAGE_PATH}>
              Register
            </Link>
          </label>
        </div>
      </form>
    </div>
  );
}

export default LoginForm;
