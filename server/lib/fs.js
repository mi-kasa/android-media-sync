const fs = require('fs-extra');
const path = require('path');
const debug = require('debug')('fs');
const jsonfile = require('jsonfile');

function getDefaultUploadDirectory(conf) {
  let config = Object.assign({
    default: '../uploads'
  }, conf || {});

  let dir = path.isAbsolute(config.default) ? 
    config.default : 
    path.join(__dirname, config.default);

  debug('Default upload directory ', dir);
  return dir;
}

function ensureDefaultUploadDirectory(conf) {
  let dir = getDefaultUploadDirectory(conf);

  fs.ensureDir(dir);
}

function postProcessUpload(metadata) {
  debug('Post processing file ', metadata);
  let current = metadata.path;
  let destination = path.join(metadata.destination, metadata.originalname);
  debug(`Moving from ${current} to ${destination}`);
  
  return new Promise((resolve, reject) => {
    // Move file final destination
    fs.move(current, destination, {clobber: true}, (err) => {
      if (err) {
        reject(err);
      }
      resolve();
    });
  }).then(() => {
    let dirName = path.dirname(destination);
    let newFile = path.join(dirName, `${metadata.title}.json`);
    debug(`Generating new json file ${newFile}`);
    return new Promise((resolve, reject) => {
      jsonfile.writeFile(newFile, metadata, (err) => {
        if (err) {
          reject(err);
        }
        resolve();
      });
    });
  });

}

module.exports = {
  getDefaultUploadDirectory,
  ensureDefaultUploadDirectory,
  postProcessUpload
};
