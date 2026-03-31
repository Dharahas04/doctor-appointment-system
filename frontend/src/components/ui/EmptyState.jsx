function EmptyState({ detail, title }) {
  return (
    <div className="empty-state">
      <strong>{title}</strong>
      <p>{detail}</p>
    </div>
  );
}

export default EmptyState;
