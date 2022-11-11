function loginApi(data) {
  //通过ajax向后端发送请求
  return $axios({
    'url': '/employee/login',
    'method': 'post',
    data //请求数据，这里传过来的是登录表单中的数据
  })
}

function logoutApi(){
  return $axios({
    'url': '/employee/logout',
    'method': 'post',
  })
}
