const express = require('express');
const multer  = require('multer');
const fs = require('./lib/fs');
fs.ensureDefaultUploadDirectory();
const upload = multer({ dest: fs.getDefaultUploadDirectory() });
const debug = require('debug')('upload');

const app = express();

app.post('/upload', upload.array('image', 1), function(req, res, next) {
  const file = req.files[0];
  var metadata = Object.assign({}, file);
  metadata = Object.assign(metadata, req.body);

  fs.postProcessUpload(metadata).
    then(() => {
      debug("Upload finished ok");
      res.send("OK");
    }).
    catch(reason => {
      debug.error(reason);
      res.status(500).send(reason);
    });
});

app.get('/ping', function(req, res, next) {
  debug('Sending pong');
  res.send("pong");
});

app.use(function (err, req, res, next) {
  debug(err.stack);
  res.status(500).send(err);
});

module.exports = app;
