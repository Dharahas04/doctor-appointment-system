import { BrowserRouter as Router, Routes, Route, Link } from "react-router-dom";

// Navbar Component
function Navbar() {
  return (
    <nav style={{ padding: "10px", background: "#f0f0f0" }}>
      <Link to="/" style={{ marginRight: "10px" }}>Home</Link>
      <Link to="/doctors" style={{ marginRight: "10px" }}>Doctors</Link>
      <Link to="/appointment" style={{ marginRight: "10px" }}>Appointment</Link>
      <Link to="/login" style={{ marginRight: "10px" }}>Login</Link>
      <Link to="/register">Register</Link>
    </nav>
  );
}

// Pages
function Home() {
  return (
    <div>
      <h1>Doctor Appointment System</h1>
      <p>Welcome! Book appointments easily.</p>
    </div>
  );
}

function Login() {
  return (
    <div>
      <h2>Login</h2>
      <input placeholder="Email" /><br /><br />
      <input type="password" placeholder="Password" /><br /><br />
      <button>Login</button>
    </div>
  );
}

function Register() {
  return (
    <div>
      <h2>Register</h2>
      <input placeholder="Name" /><br /><br />
      <input placeholder="Email" /><br /><br />
      <input type="password" placeholder="Password" /><br /><br />
      <button>Register</button>
    </div>
  );
}

function Doctors() {
  return (
    <div>
      <h2>Doctors List</h2>
      <ul>
        <li>Dr. John - Cardiologist</li>
        <li>Dr. Smith - Dentist</li>
        <li>Dr. Anna - Dermatologist</li>
      </ul>
    </div>
  );
}

function Appointment() {
  return (
    <div>
      <h2>Book Appointment</h2>
      <input type="date" /><br /><br />
      <input type="time" /><br /><br />
      <button>Book Appointment</button>
    </div>
  );
}

// Main App
function App() {
  return (
    <Router>
      <Navbar />
      <div style={{ padding: "20px" }}>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/doctors" element={<Doctors />} />
          <Route path="/appointment" element={<Appointment />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;