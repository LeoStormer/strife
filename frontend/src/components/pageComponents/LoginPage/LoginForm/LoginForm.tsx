import { type SubmitEventHandler } from "react";
import { Link } from "react-router-dom";
import formStyles from "../../../../styles/Form.module.css";
import { REGISTRATION_PAGE_PATH } from "../../../../constants";
import useFocusOnMount from "../../../../contexts/useFocusOnMount";
import StyleComposer from "../../../../utils/StyleComposer";

type LoginFormProps = {
  handleSubmit: SubmitEventHandler<HTMLFormElement>;
  isLoggingIn: boolean;
};

function LoginForm({ handleSubmit, isLoggingIn }: LoginFormProps) {
  const focusRef = useFocusOnMount();

  const linkClassName = StyleComposer(formStyles.link, {
    [formStyles.disabled as string]: isLoggingIn,
  });

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
            ref={focusRef}
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
          <Link className={`${formStyles.label} ${linkClassName}`} to='#'>
            Forgot your password?
          </Link>
        </div>
        <div>
          <button
            className={formStyles.button}
            type='submit'
            disabled={isLoggingIn}
          >
            {isLoggingIn ? "Logging in..." : "Log In"}
          </button>
          <div className={formStyles.label}>
            Need an Account?{" "}
            <Link
              className={linkClassName}
              to={isLoggingIn ? "#" : REGISTRATION_PAGE_PATH}
            >
              Register
            </Link>
          </div>
        </div>
      </form>
    </div>
  );
}

export default LoginForm;
