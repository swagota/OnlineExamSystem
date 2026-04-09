<h1 align="center">🎓 EXAMORA</h1>

<p align="center">
  <b>A modern desktop-based online examination system built with JavaFX, FXML, MySQL, and JDBC.</b>
</p>

<p align="center">
  EXAMORA is designed to make exam management easier for teachers and the exam experience smoother for students through timed assessments, instant result generation, detailed answer review, performance tracking, and guardian email notification.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-JavaFX-orange?style=for-the-badge" alt="JavaFX Badge" />
  <img src="https://img.shields.io/badge/Database-MySQL-blue?style=for-the-badge" alt="MySQL Badge" />
  <img src="https://img.shields.io/badge/UI-FXML-purple?style=for-the-badge" alt="FXML Badge" />
  <img src="https://img.shields.io/badge/Connectivity-JDBC-green?style=for-the-badge" alt="JDBC Badge" />
  <img src="https://img.shields.io/badge/Build-Gradle-darkgreen?style=for-the-badge" alt="Gradle Badge" />
</p>

<hr>

<h2>📌 About the Project</h2>

<p>
  <b>EXAMORA</b> is a role-based online exam management application developed for academic use.
  It provides separate functionalities for teachers and students, making the entire examination process more organized, interactive, and efficient.
</p>

<p>
  Teachers can create exams, define duration, add and manage multiple-choice questions, and review student outcomes.
  Students can browse available exams, attend timed MCQ tests, receive instant results, and review their submitted answers in a detailed and visual way.
</p>

<hr>

<h2>✨ Core Features</h2>

<table>
  <tr>
    <td><b>👨‍🏫 Teacher Panel</b></td>
    <td>Create exams, set duration, add questions, edit questions, delete questions, and manage exam data.</td>
  </tr>
  <tr>
    <td><b>🧑‍🎓 Student Panel</b></td>
    <td>View available exams, start tests, answer MCQs, submit responses, and review results.</td>
  </tr>
  <tr>
    <td><b>⏱ Timed Examination</b></td>
    <td>Each exam runs with a countdown timer and auto-submits when time runs out.</td>
  </tr>
  <tr>
    <td><b>📊 Instant Result Processing</b></td>
    <td>Results are generated instantly after submission and stored in the database.</td>
  </tr>
  <tr>
    <td><b>➖ Negative Marking</b></td>
    <td>Correct answer = +1, Wrong answer = -0.25, Skipped = 0.</td>
  </tr>
  <tr>
    <td><b>📝 Answer Review</b></td>
    <td>Submitted answers can be reviewed with correct, wrong, and skipped options visually highlighted.</td>
  </tr>
  <tr>
    <td><b>📈 Performance &amp; Leaderboard</b></td>
    <td>Supports result analysis and comparative performance tracking.</td>
  </tr>
  <tr>
    <td><b>📩 Guardian Email Notification</b></td>
    <td>Exam result summaries can be sent to guardians through email after submission.</td>
  </tr>
</table>

<hr>

<h2>🛠️ Technology Stack</h2>

<ul>
  <li><b>Language:</b> Java</li>
  <li><b>Framework:</b> JavaFX</li>
  <li><b>UI Design:</b> FXML</li>
  <li><b>Database:</b> MySQL</li>
  <li><b>Database Connectivity:</b> JDBC</li>
  <li><b>Build Tool:</b> Gradle</li>
  <li><b>Email Integration:</b> Jakarta Mail</li>
</ul>

<hr>

<h2>⚙️ How EXAMORA Works</h2>

<h3>Teacher Workflow</h3>
<p>
  Teacher Dashboard → Create Exam → Add Questions → View / Edit / Delete Questions → Review Student Results
</p>

<h3>Student Workflow</h3>
<p>
  Student Dashboard → View Available Exams → Start Exam → Answer Questions → Submit Exam → View Result → Review Answers
</p>

<h3>Internal Logic Overview</h3>
<ul>
  <li>Exam information is stored in the <b>exams</b> table.</li>
  <li>Questions are stored exam-wise in the <b>questions</b> table.</li>
  <li>Final score summaries are stored in the <b>results</b> table.</li>
  <li>Detailed answer-by-answer review data is stored in the <b>result_answers</b> table.</li>
  <li>Guardian email notifications are sent after exam submission using email integration.</li>
</ul>

<hr>

<h2>🧩 Main Modules</h2>

<ul>
  <li><b>Exam Creation Module</b> – Used by teachers to create and configure exams.</li>
  <li><b>Question Management Module</b> – Supports adding, editing, viewing, and deleting MCQ questions.</li>
  <li><b>Exam Attempt Module</b> – Allows students to attend an exam with a single-answer MCQ interface.</li>
  <li><b>Result Module</b> – Calculates and stores final scores.</li>
  <li><b>Review Module</b> – Displays submitted answers with proper feedback.</li>
  <li><b>Performance Module</b> – Helps monitor student progress and ranking.</li>
</ul>

<hr>

<h2>🗄️ Database Structure</h2>

<ul>
  <li><b>exams</b> – stores exam title, duration, marks, and teacher information</li>
  <li><b>questions</b> – stores question text, four options, and correct answer for each exam</li>
  <li><b>results</b> – stores result summary after a student submits an exam</li>
  <li><b>result_answers</b> – stores question-wise answer history for detailed review</li>
</ul>

<hr>

<h2>🌟 Highlighted Functionalities</h2>

<ul>
  <li>Dynamic loading of exam questions from the database</li>
  <li>Single-answer MCQ selection using grouped radio buttons</li>
  <li>Countdown timer for every exam</li>
  <li>Automatic submission when time expires</li>
  <li>Color-coded answer review interface</li>
  <li>Stored result history for future review</li>
</ul>

<h2>🚀 Future Improvements</h2>

<ul>
  <li>Question randomization</li>
  <li>More advanced analytics and charts</li>
  <li>Exportable exam reports</li>
  <li>Improved discussion and communication support</li>
  <li>More flexible marking configuration</li>
</ul>

<hr>

<h2>👩‍💻 Developed By</h2>

<p>
  <b>Swagota Saha</b><br>
  Project Developer
</p>

<p>
  <b>Labiba Tasneem</b><br>
  Project Partner
</p>

<p>
  <b>Supervisor:</b> Koushik Roy Sir
</p>

<hr>

<h2>🙏 Acknowledgement</h2>

<p>
  We are sincerely grateful to our respected supervisor, <b>Koushik Roy Sir</b>, for his valuable guidance, ideas, continuous motivation, and constructive encouragement throughout the development of this project.
</p>

<p>
  Special thanks to <b>Labiba Tasneem</b> for being a constant source of support, patience, and calmness during stressful moments, and for contributing meaningfully throughout the journey.
</p>

<p>
  This project reflects not only technical effort, but also teamwork, persistence, learning, and shared dedication.
</p>

<hr>

<h2>📬 Repository</h2>

<p>
  <a href="https://github.com/swagota/OnlineExamSystem">View Project Repository</a>
</p>

<hr>

<p align="center">
  Made with dedication, patience, learning, and teamwork 💙
</p>