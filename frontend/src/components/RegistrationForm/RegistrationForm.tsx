import { type FormEventHandler } from "react";
import { Link } from "react-router-dom";
import formStyles from "../../styles/Form.module.css";
import { LOGIN_PAGE_PATH } from "../../constants";

type RegistrationFormProps = {
  handleSubmit: FormEventHandler<HTMLFormElement>;
};

function RegistrationForm({ handleSubmit }: RegistrationFormProps) {
  return (
    <div className={formStyles.container}>
      <form className={formStyles.form} onSubmit={handleSubmit}>
        <header className={formStyles.header}>
          <h2>Create an Account</h2>
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
          <label className={formStyles.inputLabel} htmlFor='username'>
            Username
          </label>
          <input
            className={formStyles.input}
            type='text'
            name='username'
            id='username'
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
        </div>
        <div className={formStyles.label}>
          Lorem ipsum dolor sit amet, consectetur adipiscing{" "}
          <span className={formStyles.link}>elit sed vel</span> neque
          nec risus tristique condimentum{" "}
          <span className={formStyles.link}>Pellentesque finibus</span>
        </div>
        <div>
          <button className={formStyles.button} type='submit'>
            Create Account
          </button>
          <Link
            className={`${formStyles.link} ${formStyles.label}`}
            to={LOGIN_PAGE_PATH}
          >
            Already have an account? Log in
          </Link>
        </div>
      </form>
    </div>
  );
}

export default RegistrationForm;
