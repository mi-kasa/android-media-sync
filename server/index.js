const express = require('express')
const multer  = require('multer')
const upload = multer({ dest: 'uploads/' })
const jsonfile = require('jsonfile')
const path = require('path')
const fs = require('fs-extra')
const debug = require('debug')('upload', true)

const app = express()

app.post('/upload', upload.array('image', 1), function(req, res, next) {
  const file = req.files[0]
  debug('Got new file to upload ', file.originalname);
  const filePath = file.path
  const destination = path.join(file.destination, file.originalname)
  debug('Destination ', destination);
  var metadata = Object.assign({}, req.file)
  metadata = Object.assign(metadata, req.body)
  debug('Metadata ', metadata);

  fs.move(filePath, destination, (err) => {
    if (err) {
      debug("Error while moving file ", err);
      return res.status(500).send(err)
    }

    jsonfile.writeFile(`uploads/${metadata.title}.json`, metadata, (err) => {
      debug(`File ${metadata.title} uploaded correctly`)
      res.send("OK")
    })

  });
})

app.get('/ping', function(req, res, next) {
  debug('Sending pong')
  res.send("pong")
})

app.use(function (err, req, res, next) {
  debug(err.stack)
  res.status(500).send(err)
})

app.listen(3000, '192.168.0.15', function () {
  debug('Upload server listening on port 3000!')
})
