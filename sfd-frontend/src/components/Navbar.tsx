import { Link } from "react-router-dom";

const Navbar = () => {
  return (
    <nav className="p-4 bg-gray-800 text-white flex gap-4">
      <Link to="/">Dashboard</Link>  {/* Link is used to navigate between pages in a React app without reloading the page. */}
      <Link to="/workflows">Workflows</Link>
      <Link to="/failures">Failures</Link>
    </nav>
  );
};

export default Navbar;
