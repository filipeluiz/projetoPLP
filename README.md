#Projeto de Paradigmas de Linguagem de Programação ̃
Interpretador de Linguagem de Programação ̃

Resumo

O objetivo desse trabalho e desenvolver um interpretador para um subconjunto de uma linguagem de programação
conhecida. Para isso cada equipe deve escolher uma linguagem de programação. As equipes NÂO irão desen-  ̃
volver um projeto para toda a linguagem escolhida. O interpretador deve executar somente os um subconjunto
dos elementos.

##Elementos Basicos  ́
A linguagem possui as seguintes características:
• Comandos
– Comandos condicionais: no mínimo, IF e ELSE. Caso a linguagem nao possua ELSE, ele não precisa  ̃
ser implementado. Caso a linguagem tenha comandos como ELIF, a interpretação será OPCIONAL.  ́
– Comandos de repetição: no mínimo, comando baseado em contagem (FOR) e comando baseado em
condição (WHILE). Caso a linguagem possua algum desses comandos, eles NÂO serão implementados.  ̃
Mas a equipe DEVE mostrar que não há nenhum comando equivalente, por exemplo, FORTRAN 95  ́
não possui a palavra reservada FOR, mas possui o comando de repetição baseado em contagem. E se  ̃
a linguagem permitir mais de uma construção para esses comandos, a equipe deve considerar somente  ̃
uma. Por exemplo, a JAVA possui WHILE e DO WHILE, entao a equipe pode implementar somente  ̃
uma delas.
– Comandos de entrada e saída: no mínimo, 1 (UM) comando entrada e 1 (UM) comando saída. Caso
a linguagem permitir mais de uma construção para esses comandos, a equipe deve considerar somente  ̃
uma. Por exemplo, a JAVA possui PRINT e PRINTLN, entao a equipe pode implementar somente uma  ̃
delas.
– Comandos de atribuição: no mínimo, 1 (UM) comando de atribuição (=). Ele deve atribuir os valores
das expressoes do lado direito às expressões do lado esquerdo. As quantidades de ambos os lados devem  ̃
bater e terminar o fim da linha (que pode ser ; ou ENTER, a depender da linguagem de programação).  ̃
• Constantes:
– Inteiro: numero formado por dígitos.
– String: uma sequencia de caracteres entre aspas simples. ˆ
1

– Arranjos: elementos separados por vírgulas entre colchetes, ou gerados pelos operadores .. (dois pon-
tos) ou ... (tres pontos). ˆ

– Logico: operações de comparações que obtem um valor lógico (não podem ser armazenados em  ̃
variaveis).  ́
• Valores:
– Variaveis (começam com  _ou letras, seguidos de _ letras ou d ́ıgitos).
– Literais (inteiros, strings e arranjos).
• Operadores:
– Inteiro: + (adição), - (subtração), * (multiplicação),  ̃ / (divisao) e % (resto).  ̃
– String: + (concatenação)  ̃
– Logico:  ́ == (igual entre inteiros, strings), != (diferenc ̧a ente inteiros ou strings), ¡ (menor entre inteiros),
¿ (maior entre inteiros), ¡= (menor igual entre inteiros), ¿= (maior igual entre inteiros), === (contem ́
inteiro ou string em um arranjo), not (negação).  ̃
– Conector: and (E logico), or (OU lógico).  ́
• Função:  ̃
– .length: obter o tamanho de um arranjo.

Execucão ̃

Deve ser desenvolvido um interpretador em linha de comando que recebe um programa-fonte na linguagem esco-
lhida como argumento e executa os comandos especificados pelo programa. A saída do programa deve ser a mesma

que se fosse executado no original.
O programa devera abortar sua execução, em caso de qualquer erro léxico, sintático ou semântico. Mas NÃO ̃é da  ́
responsabilidade da equipe dizer qual o erro específico (lexico, semântico ou sintático). Somente uma mensagem  ́
de erro padrao deve ser apresentada.  ̃

Avaliação ̃
O trabalho deve ser feito em grupo de ate cinco alunos, sendo esse limite superior estrito. O trabalho será avaliado  ́
em 10 pontos, onde essa nota sera multiplicada por um fator entre 0.0 e 1.0 para compor a nota de cada aluno  ́
individualmente.
Trabalhos copiados, parcialmente ou integralmente, serao avaliados com nota ZERO. Você é responsável pela  ́
segurança de seu codigo, não podendo alegar que outro grupo o utilizou sem o seu consentimento.
