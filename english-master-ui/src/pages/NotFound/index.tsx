import { useNavigate } from "react-router-dom";

export default function NotFound() {
  const navigate = useNavigate();

  return (
    <div>
      <div>
        <h1>404</h1>
        <h2>Page not found</h2>
        <p>
          Sorry, the page you are looking for do not exist or has been moved.
        </p>

        <button onClick={() => navigate("/")}>Go back home</button>
      </div>
    </div>
  );
}
