import { Navigate } from "react-router-dom";

function ProtectedRoute({ allowedRoles, children, session }) {
  if (!session) {
    return <Navigate to="/auth" replace />;
  }

  if (allowedRoles?.length && !allowedRoles.includes(session.role)) {
    return <Navigate to={session.role === "Admin" ? "/admin" : "/patient"} replace />;
  }

  return children;
}

export default ProtectedRoute;
