import { BrowserRouter, Route, Routes } from "react-router-dom";
import Dashboard from "./pages/Dashboard";
import WorkflowDetails from "./pages/WorkflowDetails";
import Workflows from "./pages/Workflows";
import RecoveryPage from "./pages/RecoveryPage";
import EventsTesterPage from "./pages/EventsTesterPage";

function App() {
  return (
    <BrowserRouter>
    <Routes>
      <Route path="/" element={<Dashboard></Dashboard>}></Route>
      <Route path="/workflows" element={<Workflows />} />
      <Route path="/workflows/:id" element={<WorkflowDetails />} />
       <Route
          path="/workflows/:workflowId/recovery/:stepName"
          element={<RecoveryPage />}
        />
        <Route path="/events" element={<EventsTesterPage/>}/>

    </Routes>
    </BrowserRouter>
  );
}

export default App;
