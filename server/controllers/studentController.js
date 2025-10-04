const db = require('../models');

exports.getStudentsByClassId = (req, res) => {
    const classId = req.params.classId;
    const sql = 'SELECT * FROM students WHERE classId = ?';

    db.query(sql, [classId], (err, result) => {
        if (err) {
            console.error('Database query error:', err);
            return res.status(500).json({ message: 'Internal server error' });
        }

        console.log('Student list:', result);
        res.status(200).json(result);
    });
};

exports.createStudent = (req, res) => {
    const { classId, studentName, studentId } = req.body;
    const sql = 'INSERT INTO students (classId, studentName, studentId) VALUES (?, ?, ?)';

    db.query(sql, [classId, studentName, studentId], (err, result) => {
        if (!err) res.status(201).json({ message: 'Student created successfully' });
    });
};
