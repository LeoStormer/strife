import RegistrationForm from "../components/RegistrationForm";

function RegristrationPage() {
  const handleSubmit = (e) => {
    e.preventDefault();
    const formdata = new FormData(e.currentTarget);
    const payload = Object.fromEntries(formdata.entries());
    // Given the formdata, send a register request to the backend.
    // On success navigate to the user's friend page.
    // On failure show an error message.
  };

  return (
    <div>
      <RegistrationForm handleSubmit={handleSubmit}/>
    </div>
  );
}

export default RegristrationPage;
