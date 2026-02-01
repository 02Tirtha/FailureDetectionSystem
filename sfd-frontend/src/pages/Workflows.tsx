import { useWorkflows } from "../hooks/useWorkflows";
import { Link } from "react-router-dom";

const Workflows = () => {
  const { workflows, loading } = useWorkflows();

  if (loading) return <p>Loading...</p>;

  return (
    <div className="p-4">
      <h2 className="text-xl font-semibold">Workflows</h2>

      <ul className="mt-4 space-y-2">
        {workflows.map(w => (
          <li key={w.id} className="border p-2 rounded">
            <Link to={`/workflows/${w.id}`}>
              {w.name}
            </Link>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default Workflows;
