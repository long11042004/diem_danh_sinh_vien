const express = require('express');
const bodyParser = require('body-parser');
const cors = require('cors');
const jwt = require('jsonwebtoken');
const bcrypt = require('bcrypt');
const mysql = require('mysql');

const app = express();
const port = 3000;

app.use(cors());
app.use(bodyParser.json());

const db = mysql.createConnection({
    host: 'localhost',
    insecureAuth : true,
    user: 'root',
    password: '20225359',
    database: 'diemdanhsinhvien',
    ssl: {
        rejectUnauthorized: false
    }
});

db.connect((err) => {
    if (err) {
        console.error('Lỗi kết nối cơ sở dữ liệu:', err);
    } else {
        console.log('Đã kết nối đến cơ sở dữ liệu MySQL');
    }
});

app.post('/register', async (req, res) => {
    const { username, password, email, fullName } = req.body;
  
    // Kiểm tra xem người dùng đã tồn tại chưa
    db.query('SELECT * FROM users WHERE username = ? OR email = ?', [username, email], (err, result) => {
        if (err) {
            console.error('Lỗi truy vấn cơ sở dữ liệu:', err);
            return res.status(500).json({ message: 'Lỗi máy chủ nội bộ' });
        }
  
        if (result.length > 0) {
            return res.status(400).json({ message: 'Tên người dùng hoặc email đã tồn tại' });
        }
  
        // Nếu chưa tồn tại, thêm người dùng mới
        db.query('INSERT INTO users (username, password, email, full_name) VALUES (?, ?, ?, ?)', [username, password, email, fullName], (err, result) => {
            if (err) {
                console.error('Lỗi truy vấn cơ sở dữ liệu:', err);
                return res.status(500).json({ message: 'Lỗi máy chủ nội bộ' });
            }
  
            console.log('Người dùng đã được tạo:', result);
            res.status(201).json({ message: 'Người dùng đã được tạo thành công' });
        });
    });
});

app.post('/login', (req, res) => {
    const { username, password } = req.body;

    // Tìm người dùng trong cơ sở dữ liệu
    db.query('SELECT * FROM users WHERE username = ?', [username], (err, result) => {
        if (err) {
            console.error('Lỗi truy vấn cơ sở dữ liệu: ', err);
            return res.status(500).json({ message: 'Lỗi máy chủ nội bộ' });
        }

        if (result.length === 0) {
             return res.status(401).json({ message: 'Tên đăng nhập hoặc mật khẩu không đúng' });
        }

        const user = result[0];

        // So sánh mật khẩu đã nhập với mật khẩu đã mã hóa trong cơ sở dữ liệu
        bcrypt.compare(password, user.password, (err, isMatch) => {
            if (err) {
                console.error('Lỗi so sánh mật khẩu: ', err);
                return res.status(500).json({ message: 'Lỗi máy chủ nội bộ' });
            }

            if (!isMatch) {
                return res.status(401).json({ message: 'Tên đăng nhập hoặc mật khẩu không đúng' });
            }
          
            // Tạo JWT token
            const token = jwt.sign({ userId: user.id, username: user.username }, 'your_secret_key', { expiresIn: '1h' }); // expires in 1 hour

            res.status(200).json({ message: 'Đăng nhập thành công', token: token });
        });
    });
});

const authenticateToken = (req, res, next) => {
    const authHeader = req.headers['authorization'];
    const token = authHeader && authHeader.split(' ')[1];

    if (token == null) {
        return res.sendStatus(401); // Unauthorized
    }

    jwt.verify(token, 'your_secret_key', (err, user) => {
        if (err) {
            return res.sendStatus(403); // Forbidden
        }

        req.user = user;
        next(); // Cho phép yêu cầu tiếp tục
    });
};

app.get('/classes', (req, res) => {
    const sql = 'SELECT * FROM classes';

    db.query(sql, (err, result) => {
        if (err) {
            console.error('Lỗi truy vấn cơ sở dữ liệu:', err);
            return res.status(500).json({ message: 'Lỗi máy chủ nội bộ' });
        }
  
        console.log('Danh sách lớp học:', result);
        res.status(200).json(result);
    });
});

app.post('/classes', (req, res) => {
    const { courseName, classCode, courseId, semester, scheduleInfo } = req.body;
    const sql = 'INSERT INTO classes (courseName, classCode, courseId, semester, scheduleInfo) VALUES (?, ?, ?, ?, ?)';

    db.query(sql, [courseName, classCode, courseId, semester, scheduleInfo], (err, result) => {
        if (err) {
            console.error('Lỗi truy vấn cơ sở dữ liệu:', err);
            return res.status(500).json({ message: 'Lỗi máy chủ nội bộ' });
        }

        console.log('Lớp học đã được thêm:', result);
        res.status(201).json({ message: 'Lớp học đã được thêm thành công' });
    });
});

app.get('/students/:classId', (req, res) => {
    const classId = req.params.classId;
    const sql = 'SELECT * FROM students WHERE classId = ?';

    db.query(sql, [classId], (err, result) => {
        if (err) {
            console.error('Lỗi truy vấn cơ sở dữ liệu:', err);
            return res.status(500).json({ message: 'Lỗi máy chủ nội bộ' });
        }

        console.log('Danh sách sinh viên:', result);
        res.status(200).json(result);
    });
});

app.post('/students', (req, res) => {
    const { classId, studentName, studentId } = req.body;
    const sql = 'INSERT INTO students (classId, studentName, studentId) VALUES (?, ?, ?)';

    db.query(sql, [classId, studentName, studentId], (err, result) => {
        if (err) {
            console.error('Lỗi truy vấn cơ sở dữ liệu:', err);
            return res.status(500).json({ message: 'Lỗi máy chủ nội bộ' });
        }

        console.log('Sinh viên đã được thêm:', result);
        res.status(201).json({ message: 'Sinh viên đã được thêm thành công' });
    });
});

app.get('/attendance_sessions/:classId', (req, res) => {
    const classId = req.params.classId;
    const sql = 'SELECT * FROM attendance_sessions WHERE classId = ?';

    db.query(sql, [classId], (err, result) => {
        if (err) {
            console.error('Lỗi truy vấn cơ sở dữ liệu:', err);
            return res.status(500).json({ message: 'Lỗi máy chủ nội bộ' });
        }

        console.log('Danh sách attendance_sessions:', result);
        res.status(200).json(result);
    });
});

app.post('/attendance_sessions', (req, res) => {
    const { classId, date } = req.body;
    const sql = 'INSERT INTO attendance_sessions (classId, date) VALUES (?, ?)';

    db.query(sql, [classId, date], (err, result) => {
        if (err) {
            console.error('Lỗi truy vấn cơ sở dữ liệu:', err);
            return res.status(500).json({ message: 'Lỗi máy chủ nội bộ' });
        }

        console.log('attendance_sessions đã được thêm:', result);
        res.status(201).json({ message: 'attendance_sessions đã được thêm thành công' });
    });
});

app.post('/attendance_records', (req, res) => {
    const { sessionId, studentId, status } = req.body;
    const sql = 'INSERT INTO attendance_records (sessionId, studentId, status) VALUES (?, ?, ?)';

    db.query(sql, [sessionId, studentId, status], (err, result) => {
        if (err) {
            console.error('Lỗi truy vấn cơ sở dữ liệu:', err);
            return res.status(500).json({ message: 'Lỗi máy chủ nội bộ' });
        }

        console.log('attendance_records đã được thêm:', result);
        res.status(201).json({ message: 'attendance_records đã được thêm thành công' });
    });
});

app.listen(port, () => {
    console.log(`Máy chủ đang hoạt động tại cổng ${port}...`);
});
