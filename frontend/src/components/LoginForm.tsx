import { type FormEventHandler } from "react";
import { Link } from "react-router-dom";

type LoginFormProps = {
  handleSubmit: FormEventHandler<HTMLFormElement>;
};

function LoginForm({ handleSubmit }: LoginFormProps) {
  return (
    <div className='form-container'>
      <form className='login-form' onSubmit={handleSubmit}>
        <div className='wrapper'>
          <div className='form-group'>
            <label htmlFor='email'>Email</label>
            <br />
            <input type='email' name='email' id='email' required />
          </div>
          <div className='form-group'>
            <label htmlFor='password'>Password</label>
            <br />
            <input type='password' name='password' id='password' required />
          </div>
          <label>Forgot your password?</label>
          <div className='form-group'>
            <button type='submit'>Login</button>
            <br />
            <label>
              Need an Account? <Link to='/register'>Register</Link>
            </label>
          </div>
        </div>
      </form>
    </div>
  );
}

export default LoginForm;
