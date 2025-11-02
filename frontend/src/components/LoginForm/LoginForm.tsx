import { type FormEventHandler } from "react";
import { Link } from "react-router-dom";
import formStyles from "../../styles/Form.module.css";
import styles from "./LoginForm.module.css";

type LoginFormProps = {
  handleSubmit: FormEventHandler<HTMLFormElement>;
};

function LoginForm({ handleSubmit }: LoginFormProps) {
  return (
    <div className={formStyles.container}>
      <form className={styles.form} onSubmit={handleSubmit}>
        <div className={formStyles.wrapper}>
          <div className={formStyles.formGroup}>
            <label className={formStyles.label} htmlFor='email'>
              Email
            </label>
            <br />
            <input
              className={formStyles.input}
              type='email'
              name='email'
              id='email'
              required
            />
          </div>
          <div className={formStyles.formGroup}>
            <label className={formStyles.label} htmlFor='password'>
              Password
            </label>
            <br />
            <input
              className={formStyles.input}
              type='password'
              name='password'
              id='password'
              required
            />
          </div>
          <label className={formStyles.label}>Forgot your password?</label>
          <div className={formStyles.formGroup}>
            <button className={formStyles.button} type='submit'>
              Login
            </button>
            <br />
            <label className={formStyles.label}>
              Need an Account? <Link to='/register'>Register</Link>
            </label>
          </div>
        </div>
      </form>
    </div>
  );
}

export default LoginForm;
