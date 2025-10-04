const db = require('../models'); // Import the database connection

exports.getAllClasses = (req, res) => {
    const sql = 'SELECT * FROM classes';

    db.query(sql, (err, result) => {
        if (err) {
            console.error('Database query error:', err);
            return res.status(500).json({ message: 'Internal server error' });
        }

        console.log('Class list:', result);
        res.status(200).json(result);
    });
};

exports.createClass = (req, res) => {
    const { courseName, classCode, courseId, semester, scheduleInfo } = req.body;
    const sql = 'INSERT INTO classes (courseName, classCode, courseId, semester, scheduleInfo) VALUES (?, ?, ?, ?, ?)';

    db.query(sql, [courseName, classCode, courseId, semester, scheduleInfo], (err, result) => {
        if (!err) res.status(201).json({ message: 'Class added successfully' });
    });
};
