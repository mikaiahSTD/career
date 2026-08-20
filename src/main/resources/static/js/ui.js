(function (window) {
  'use strict';

  var LOGIN_URL = '/ui/login';
  var XSRF_COOKIE = 'XSRF-TOKEN';
  var XSRF_HEADER = 'X-XSRF-TOKEN';

  var currentUser = null;

  function readCookie(name) {
    var match = document.cookie.match('(?:^|;)\\s*' + name + '\\s*=\\s*([^;]*)');
    return match ? decodeURIComponent(match[1]) : null;
  }

  function readCsrfToken() {
    var meta = document.querySelector('meta[name="_csrf"]');
    if (meta && meta.getAttribute('content')) {
      return meta.getAttribute('content');
    }
    return readCookie(XSRF_COOKIE);
  }

  var Auth = {
    getUser: function () {
      return currentUser;
    },
    getRole: function () {
      return currentUser ? currentUser.role : null;
    },
    getUserId: function () {
      return currentUser ? currentUser.id : null;
    },
    getEmail: function () {
      return currentUser ? currentUser.email : null;
    },
    isAuthenticated: function () {
      return !!currentUser;
    },
    requireAuth: function () {
      return CareerUI.apiJson('/auth/me')
        .then(function (me) {
          currentUser = me;
          return me;
        })
        .catch(function (err) {
          if (err.status === 401 || err.status === 403) {
            currentUser = null;
            window.location.replace(LOGIN_URL);
            return null;
          }
          throw err;
        });
    },
    logout: function (redirect) {
      CareerUI.api('/auth/logout', { method: 'POST' })
        .catch(function () {})
        .then(function () {
          currentUser = null;
          window.location.replace(redirect || LOGIN_URL);
        });
    }
  };

  function esc(value) {
    var div = document.createElement('div');
    div.textContent = value == null ? '' : String(value);
    return div.innerHTML;
  }

  function statusMessage(res, fallback) {
    return res
      .json()
      .then(function (body) {
        return body && body.message ? body.message : fallback;
      })
      .catch(function () {
        return fallback;
      });
  }

  function api(path, options) {
    options = options || {};
    var method = (options.method || 'GET').toUpperCase();
    var headers = options.headers || {};
    if (options.body != null) {
      headers['Content-Type'] = 'application/json';
    }
    if (method !== 'GET' && method !== 'HEAD' && method !== 'OPTIONS') {
      var csrf = readCsrfToken();
      if (csrf) {
        headers[XSRF_HEADER] = csrf;
      }
    }
    var init = { method: method, headers: headers };
    if (options.body != null) {
      init.body = JSON.stringify(options.body);
    }
    return fetch(path, init).then(function (res) {
      if (res.ok) {
        return res;
      }
      return statusMessage(res, 'Request failed (HTTP ' + res.status + ')').then(function (message) {
        var err = new Error(message);
        err.status = res.status;
        if ((res.status === 401 || res.status === 403) && !options.noAuthRedirect) {
          currentUser = null;
          window.location.replace(LOGIN_URL);
        }
        throw err;
      });
    });
  }

  function apiJson(path, options) {
    return api(path, options).then(function (res) {
      return res.status === 204 ? null : res.json();
    });
  }

  function apiBlob(path, options) {
    return api(path, options).then(function (res) {
      return res.blob();
    });
  }

  function downloadBlob(blob, filename) {
    var url = window.URL.createObjectURL(blob);
    var a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    a.remove();
    window.URL.revokeObjectURL(url);
  }

  window.CareerUI = {
    Auth: Auth,
    esc: esc,
    api: api,
    apiJson: apiJson,
    apiBlob: apiBlob,
    downloadBlob: downloadBlob
  };
})(window);