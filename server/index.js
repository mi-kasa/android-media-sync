const express = require('express')
const multer  = require('multer')
const upload = multer({ dest: 'uploads/' })
const jsonfile = require('jsonfile')
const path = require('path')
const fs = require('fs-extra')
const debug = require('debug')('app')

const app = express()

app.post('/upload', upload.array('image', 1), function(req, res, next) {
  const file = req.files[0]
  const filePath = file.path
  const destination = path.join(file.destination, file.originalname)
  var metadata = Object.assign({}, req.file)
  metadata = Object.assign(metadata, req.body)

  fs.move(filePath, destination, (err) => {
    if (err) {
      console.log(err)
      return res.status(500).send(err)
    }

    jsonfile.writeFile(`uploads/${metadata.title}.json`, metadata, (err) => {
      res.send("OK")
    })

  });
})

app.get('/ping', function(req, res, next) {
  res.send("pong")
})

app.listen(3000, '192.168.0.15', function () {
  console.log('Upload server listening on port 3000!')
})
