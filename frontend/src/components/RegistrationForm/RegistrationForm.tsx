import { type FormEventHandler } from "react";
import { Link } from "react-router-dom";
import formStyles from "../../styles/Form.module.css";
import styles from "./RegistrationForm.module.css";

type RegistrationFormProps = {
  handleSubmit: FormEventHandler<HTMLFormElement>;
};

function RegistrationForm({ handleSubmit }: RegistrationFormProps) {
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
            <label className={formStyles.label} htmlFor='username'>
              Username
            </label>
            <br />
            <input
              className={formStyles.input}
              type='text'
              name='username'
              id='username'
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
          <div className={formStyles.formGroup}>
            <button className={formStyles.button} type='submit'>
              Create Account
            </button>
            <br />
            <Link to='/login'>Already have an account? Log in</Link>
          </div>
        </div>
      </form>
    </div>
  );
}

export default RegistrationForm;
