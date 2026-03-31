import { NavLink, Outlet } from "react-router-dom";

function AppShell({ notice, onLogout, session }) {
  const isAdmin = session?.role === "Admin";

  return (
    <div className="shell">
      <aside className="rail">
        <div className="brand-lockup">
          <span className="eyebrow">PulsePoint Clinic</span>
          <h1>{isAdmin ? "Operations Console" : "Patient Dashboard"}</h1>
          <p>
            {isAdmin
              ? "Manage appointments, availability, reports, and reminder workflows from one place."
              : "Discover doctors, pick a time slot, confirm your visit, and track appointment status."}
          </p>
        </div>

        <nav className="rail-nav">
          <NavItem to={isAdmin ? "/admin" : "/patient"}>
            {isAdmin ? "Admin Console" : "Dashboard"}
          </NavItem>
        </nav>

        <div className="rail-card">
          <span className="eyebrow">Signed In</span>
          <p>{session?.fullName}</p>
          <p>{session?.email}</p>
        </div>

        <div className="rail-card">
          <span className="eyebrow">Account Type</span>
          <p>{isAdmin ? "Administrator" : "Patient"}</p>
          <button type="button" className="ghost-button" onClick={onLogout}>
            Logout
          </button>
        </div>
      </aside>

      <main className="main-panel">
        {notice ? (
          <div className={`notice notice-${notice.type}`}>
            <strong>{notice.type === "success" ? "Success" : "Attention"}</strong>
            <span>{notice.message}</span>
          </div>
        ) : null}
        <Outlet />
      </main>
    </div>
  );
}

function NavItem({ children, to }) {
  return (
    <NavLink to={to} className={({ isActive }) => (isActive ? "nav-link active" : "nav-link")}>
      {children}
    </NavLink>
  );
}

export default AppShell;
