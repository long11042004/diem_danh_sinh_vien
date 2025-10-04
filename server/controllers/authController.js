const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
const db = require('../models');

exports.register = async (req, res) => {
    const { username, password, email, fullName } = req.body;

    db.query('SELECT * FROM users WHERE username = ? OR email = ?', [username, email], (err, result) => {
        if (err) {
            console.error('Database query error:', err);
            return res.status(500).json({ message: 'Internal server error' });
        }

        if (result.length > 0) {
            return res.status(400).json({ message: 'Username or email already exists' });
        }

        db.query('INSERT INTO users (username, password, email, full_name) VALUES (?, ?, ?, ?)', [username, password, email, fullName], (err, result) => {
            if (err) {
                console.error('Database query error:', err);
                return res.status(500).json({ message: 'Internal server error' });
            }

            console.log('User created:', result);
            res.status(201).json({ message: 'User created successfully' });
        });
    });
};

exports.login = (req, res) => {
    const { username, password } = req.body;

    db.query('SELECT * FROM users WHERE username = ?', [username], (err, result) => {
        if (err) {
            console.error('Database query error: ', err);
            return res.status(500).json({ message: 'Internal server error' });
        }

        if (result.length === 0) {
            return res.status(401).json({ message: 'Incorrect username or password' });
        }

        const user = result[0];

        bcrypt.compare(password, user.password, (err, isMatch) => {
            if (err) {
                console.error('Password comparison error: ', err);
                return res.status(500).json({ message: 'Internal server error' });
            }

            if (!isMatch) {
                return res.status(401).json({ message: 'Incorrect username or password' });
            }

            const token = jwt.sign({ userId: user.id, username: user.username }, 'your_secret_key', { expiresIn: '1h' });
            res.status(200).json({ message: 'Login successful', token: token });
        });
    });
};
