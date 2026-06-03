# Material fuente: Consideraciones adicionales generales para el backend.docx

Consideraciones adicionales generales para el 
backend
:

- En general, para los 
endpoints
 que no sean del modo 
admin
 auxiliar para pruebas, los que no sean de esos, 
recorda
 en general respetar como ya 
estan
 hechos en lo que te mande del texto de 
endpoints
, y solo cambiar/agregar cosas en esos si es necesario, pero tampoco cambiar por cambiar sin ninguna 
razon
, como para respetar eso general de la consigna, de implementar 
segun
 lo diseñado (al menos de manera general intentar respetar eso, pero 
obicamente
 IMPORTANTE, si hace falta cambiar o agregar algo para 
quer
 todo ande bien, entonces no hay problema).

- Pensando en la carpeta 
src
 y la estructura del proyecto, en la carpeta java, una propuesta de estructura que te propongo, que 
podes
 usar si te parece bien, 
seria
 la siguiente:

Tener carpetas principales de 
controller
 (
quizas
 alguna 
subdivision
 en esto si te parece acorde), 
entity
 (estas separadas entre 
basic
, 
dto
 y 
enums
 - con la de 
dto
 separada entre 
create
, 
request
, response, 
update
, etc.), 
exception
 (
quizas
 alguna 
subdivision
 en esto si te parece acorde), 
mapper
, 
repository
, 
service
 (con una división entre interface e 
implementation
, o algo de ese estilo). PERO IGUAL IMPORTANTE, 
pensa
 que esto es una idea general simplemente, y si te 
paece
 que 
seria
 mejor cambiar algo, agregar algo, agregar 
mas
 subdivisiones en alguna parte o cosas de ese estilo, no hay problema, es solo una idea general de como lo hice para otro proyecto que hice.

- En el pom.xml, si podes, solo agrega cosas, como para no modificar o sacar si no es necesario, sino simplemente completar con lo que haga falta y listo. O bueno, en 
relacion
 a eso, si te parece mejor, en lugar de agregar al pom.xml, me 
podes
 indicar que agregar yo manualmente y listo, si es que eso te parece mejor.

- 
Pensa
 que no tengo nada creado del sistema ya existente de 
EstructuraActualSQL
, entonces, si podes, hace para que este 
backend
 pueda crear eso, porque no tengo nada de eso ahora mismo en mi BD, es decir, esta 
vacia
. Y todo lo que haga falta de datos semilla o cosas por el estilo, si se puede, 
tambien
 agregarlo para que lo haga este mismo 
backend
.

- De manera general, para detalles de 
implementacion
 fina, o decisiones finas en general, de cualquier aspecto que no te haya aclarado, si podes, 
hacelo
 de la manera que te parezca mejor 
segun
 tu criterio, es decir, que no importe que en lo que te 
envie
 no estaba aclarado o definido; es decir, para esos casos, 
hacelo
 
segun
 tu criterio y listo. 
Basicamente
, que cumpla con todo lo que te mande de 
informacion
 y todo lo que venimos hablando, y para aquellos detalles 
mas
 finos, podes hacerlo 
segun
 te parezca acorde y listo, como para que ya quede un 
backend
 todo preparado; 
y
 en cualquier caso, si no me gusta como quedo, entonces yo 
despues
 lo cambio por mi cuenta y listo. Pero eso 
si
, importante, 

aclarame
  y
 
explicame
 todo eso que hagas, suposiciones que hagas, cosas que decidas por tu cuenta, etc.

- Agrega 
algun
 README en alguna parte de lo enviado, como para tener la 
informacion
 y todo bien completo de absolutamente todos los aspectos que te parezcan que debo estar informado desde el inicio 
ahi
, por 
ej
, decisiones que tomaste, como hiciste x cosas, aspectos a considerar relevantes, etc. (ESTO TAMBIEN EN REFERENCIA A ESO ULTIMO QUE TE DIJE DE ESOS DETALLES/DECISIONES MAS FINOS DE IMPLEMENTACION). Es decir, en ese README, no te olvides de 
ningun
 detalle e 
indicame
/
contame
 todo lo que consideres acorde que sepa, todo lo 
mas
 completo y mejor explicado posible. Esto 
tambien
 en parte para ver 
mas
 rápida y fácilmente si hay algo que esta implementado de una manera diferente a como lo 
tenia
 pensado. Y 
ademas
 de todo eso, si podes, en esto 
tambien
 agrega todo lo necesario para correr el 
backend
, todos los pasos para que ande perfecto, y para poder probarlo en general, pensando en probar los 
endpoints
 y 
demas
 apartados del 
backend
.