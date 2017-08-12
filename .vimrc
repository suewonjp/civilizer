" vim: sw=2:

let prjPath = expand("%:p:h")

if (filereadable("Session.vim"))
  :source Session.vim
endif

set cpt+=ksrc/**/*.java,ksrc/main/webapp/js/app/**/*.js,ktools/**/*

set path=$PWD,$PWD/src/**,$PWD/tools**,$PWD/gradle/**,$PWD/extra/**

set wildignore+=.git/**,target/**,build/**,test/**,.gradle/**,.settings/**,.metadata/**,.springBeans/**/,src/**/compressed.js,src/**/compressed.css

nnoremap <silent><leader>w :Ack! --ignore-dir='target' --ignore-dir='build' --ignore-dir='test' --ignore='compressed.*' --ignore='*[.-]min.*' -w <C-r><C-w> <CR>

nnoremap <silent><leader>g :Ack! --ignore-dir='target' --ignore-dir='build' --ignore-dir='test' --ignore='compressed.*' --ignore='*[.-]min.*' -w <C-r>" <CR>

cnoreabbrev gr Ack! --ignore-dir='target' --ignore-dir='build' --ignore-dir='test' --ignore='compressed.*' --ignore='*[.-]min.*' ''<left><C-r>=EatLastChar()<CR>

