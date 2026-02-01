import { useParams } from "react-router-dom";
import { useFailures } from "../hooks/useFailure";

const WorkflowDetails = () => {
  const { id } = useParams();
  const workflowId = Number(id);

  const { failures, loading } = useFailures(workflowId);

  if (loading) return <p>Loading...</p>;

  return (
    <div className="p-4">
      <h2 className="text-xl font-semibold">
        Workflow #{workflowId}
      </h2>

      <ul className="mt-4">
        {failures.map(f => (
          <li key={f.id}>
            {f.stepName} - {f.failureType}
          </li>
        ))}
      </ul>
    </div>
  );
};

export default WorkflowDetails;
