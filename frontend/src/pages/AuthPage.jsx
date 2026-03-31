function AuthPage({
  authTab,
  isSubmittingAuth,
  loginForm,
  notice,
  onLogin,
  onRegister,
  registerForm,
  setAuthTab,
  setLoginForm,
  setRegisterForm,
}) {
  return (
    <div className="auth-shell">
      <section className="auth-stage">
        <div className="auth-copy">
          <span className="eyebrow dark">Doctor Appointment System</span>
          <h1>Welcome to easier doctor booking.</h1>
          <p>
            Register is only for patients. Admin accounts are created manually and can access the system from login only.
          </p>
          <div className="auth-feature-list">
            <div>
              <strong>Find the right doctor</strong>
              <p>Browse specialties, compare online and offline options, and choose a convenient slot.</p>
            </div>
            <div>
              <strong>Reserve before you confirm</strong>
              <p>Time slots can be held temporarily before turning them into confirmed appointments.</p>
            </div>
            <div>
              <strong>Stay informed</strong>
              <p>Confirmation details include either a video link or clinic instructions, plus reminders.</p>
            </div>
          </div>
        </div>

        <div className="auth-card">
          <div className="tab-strip">
            <button
              type="button"
              className={authTab === "register" ? "tab-button active" : "tab-button"}
              onClick={() => setAuthTab("register")}
            >
              Register
            </button>
            <button
              type="button"
              className={authTab === "login" ? "tab-button active" : "tab-button"}
              onClick={() => setAuthTab("login")}
            >
              Login
            </button>
          </div>

          {notice ? (
            <div className={`notice notice-${notice.type}`}>
              <strong>{notice.type === "success" ? "Success" : "Attention"}</strong>
              <span>{notice.message}</span>
            </div>
          ) : null}

          {authTab === "register" ? (
            <form className="stack-form" onSubmit={onRegister}>
              <label>
                Full name
                <input
                  required
                  value={registerForm.fullName}
                  onChange={(event) =>
                    setRegisterForm((current) => ({ ...current, fullName: event.target.value }))
                  }
                  placeholder="Aarav Patel"
                />
              </label>
              <label>
                Email
                <input
                  required
                  type="email"
                  value={registerForm.email}
                  onChange={(event) =>
                    setRegisterForm((current) => ({ ...current, email: event.target.value }))
                  }
                  placeholder="aarav@example.com"
                />
              </label>
              <label>
                Password
                <input
                  required
                  type="password"
                  value={registerForm.password}
                  onChange={(event) =>
                    setRegisterForm((current) => ({ ...current, password: event.target.value }))
                  }
                  placeholder="Minimum 6 characters"
                />
              </label>
              <button type="submit" className="primary-button" disabled={isSubmittingAuth}>
                {isSubmittingAuth ? "Registering..." : "Create patient account"}
              </button>
              <p className="muted">Patient signup only. Admins should use the login tab.</p>
            </form>
          ) : (
            <form className="stack-form" onSubmit={onLogin}>
              <label>
                Email
                <input
                  required
                  type="email"
                  value={loginForm.email}
                  onChange={(event) =>
                    setLoginForm((current) => ({ ...current, email: event.target.value }))
                  }
                  placeholder="your-email@example.com"
                />
              </label>
              <label>
                Password
                <input
                  required
                  type="password"
                  value={loginForm.password}
                  onChange={(event) =>
                    setLoginForm((current) => ({ ...current, password: event.target.value }))
                  }
                  placeholder="Enter your password"
                />
              </label>
              <button type="submit" className="primary-button" disabled={isSubmittingAuth}>
                {isSubmittingAuth ? "Signing in..." : "Sign in"}
              </button>
              <p className="muted">Admin login is enabled only for manually created admin accounts.</p>
            </form>
          )}
        </div>
      </section>
    </div>
  );
}

export default AuthPage;
