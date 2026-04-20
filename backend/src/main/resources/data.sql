INSERT INTO users (email, first_name, last_name, role, active)
VALUES
  ('admin@projectpulse.local', 'Admin', 'User', 'ADMIN', true),
  ('instructor@projectpulse.local', 'Ingrid', 'Instructor', 'INSTRUCTOR', true),
  ('student1@projectpulse.local', 'Sam', 'Student', 'STUDENT', true),
  ('student2@projectpulse.local', 'Taylor', 'Student', 'STUDENT', true)
ON CONFLICT (email) DO NOTHING;

INSERT INTO rubric_criteria (name, max_score, active)
VALUES
  ('Technical Contribution', 5, true),
  ('Communication', 5, true),
  ('Reliability', 5, true)
ON CONFLICT DO NOTHING;
