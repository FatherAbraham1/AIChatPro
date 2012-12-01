README
======

## [aichatpro](https://bitbucket.org/gmochid2/aichatpro/)

Instalasi + Run
===============

Clone repo ini atau download
[latest snapshot zip][zip].

Untuk install git di Windows, download + build + install [msysgit][msysgit]
(Otomatis build dan install GCC4.4, make, bash, ssh, dsb sekali klik, agak
lama).

Cara clone yaitu:

    git clone https://bitbucket.org/gmochid2/aichatpro.git

Setelah clone/download, buka project `aichatpro` dengan netbeans, lalu run.
Secara default aplikasi akan listen di socket port 8777. Buka
http://localhost:8777/

Untuk menghasilkan jar, tinggal `Clean & Build` lewat netbeans, jar file ada di
direktori dist/. Untuk menjalankan aplikasi di port berbeda, jalankan `java -jar
dist/aichatpro.jar <port_number>`.

Penjelasan Singkat
==================

Aplikasi ini menggunakan [embedded-jetty][jetty] sebagai servlet container, dan
menggunakan [Spark][spark] sebagai web-framework. Spark meringankan beberapa
pekerjaan seperti routing url, penangangan http request dan http response.

File-file yang berada pada direktori `static/` merupakan file static file yang
dibaca oleh aplikasi (menggunakan fungsi yang ada pada `chatFile`) dan dikirimkan
langsung lewat http response. Misal, ada client request dengan url
/static/html/hello.html, maka konten dari hello.html itu dikirimkan ke client.
Aturan routing untuk static-file adalah `/static/<dir>/<file>`.

Tidak ada templating yang digunakan, aplikasi merespon client hanya dalam bentuk
plain-text dan static-file. Oleh karena itu, aplikasi ini "bermain" menggunakan
AJAX-request dari sebuah static-file html, meresponnya dalam bentuk plain-text,
dan javascript yang berada static-file html itulah yang memroses hasil respon
sehingga menjadi sebuah tampilan.

Pada contoh kali ini, routing ke root "`/`" aplikasi akan merespon sebuah
static-file html yaitu "hello.html". Pada hello.html, ada sebuah form berisi
sebuah text-input, yang jika di-submit akan melakukan AJAX-request (POST) ke /chat
dengan parameter `chat = <value dari text-input>`. Pada aplikasi, fungsi routing
ke "`/chat`" dengan request method POST, akan mengembalikan respon berupa
kebalikan dari parameter `chat` yang diberikan. Respon tersebut jika diterima
akan ditangani oleh hello.html menggunakan javascript, sehingga kebalikan dari
text-input tersebut ditampilkan.

Ide yang sama berlaku juga untuk "chatbot" sebenarnya nanti, dari text-input
client memasukkan pertanyaan, dilakukan AJAX-request ke aplikasi, aplikasi
memroses dan mengembalikan respon berupa jawaban.

Library yang Digunakan
----------------------
-   [spark][spark]
-   [jetty][jetty]
-   [bootstrap][bootstrap]
-   [jquery][jquery]

[msysgit]: http://code.google.com/p/msysgit/downloads/detail?name=msysGit-fullinstall-1.8.0-preview20121022.exe&can=2&q=
[jetty]: http://www.eclipse.org/jetty/
[spark]: http://www.sparkjava.com/
[zip]: https://bitbucket.org/gmochid2/aichatpro/get/master.zip
[bootstrap]: http://twitter.github.com/bootstrap/
[jquery]: http://jquery.com/
