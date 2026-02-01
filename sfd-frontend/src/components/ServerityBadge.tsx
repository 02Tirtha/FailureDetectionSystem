const colors: any = {
  LOW: "bg-green-500",
  MEDIUM: "bg-yellow-500",
  HIGH: "bg-red-500",
};

const SeverityBadge = ({ severity }: { severity: string }) => {
  return (
    <span className={`px-2 py-1 text-white ${colors[severity]}`}>
      {severity}
    </span>
  );
};

export default SeverityBadge;
