import { type SubmitEventHandler } from "react";
import { Link } from "react-router-dom";
import formStyles from "../../../../styles/Form.module.css";
import { LOGIN_PAGE_PATH } from "../../../../constants";
import useFocusOnMount from "../../../../contexts/useFocusOnMount";
import StyleComposer from "../../../../utils/StyleComposer";

type RegistrationFormProps = {
  handleSubmit: SubmitEventHandler<HTMLFormElement>;
  isRegistering: boolean;
};

function RegistrationForm({
  handleSubmit,
  isRegistering,
}: RegistrationFormProps) {
  const focusRef = useFocusOnMount();

  const linkClassName = StyleComposer(formStyles.link, {
    [formStyles.disabled as string]: isRegistering,
  });

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
            ref={focusRef}
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
          <span className={linkClassName}>elit sed vel</span> neque nec risus
          tristique condimentum{" "}
          <span className={linkClassName}>Pellentesque finibus</span>
        </div>
        <div>
          <button
            className={formStyles.button}
            type='submit'
            disabled={isRegistering}
          >
            {isRegistering ? "Creating Account..." : "Create Account"}
          </button>
          <Link
            className={`${linkClassName} ${formStyles.label}`}
            to={isRegistering ? "#" : LOGIN_PAGE_PATH}
          >
            Already have an account? Log in
          </Link>
        </div>
      </form>
    </div>
  );
}

export default RegistrationForm;
