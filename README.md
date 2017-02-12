# JavaMethodFeaturesCapture
A code analyzer for Java method

##概要
Javaソースファイルからメソッドの特徴を抽出します．

##解析の対象
”一番外側のクラスの直下のメソッド”です．
JDTの抽象構文木（AST）における，CompilationUnit直下のクラスの直属のメソッドのみ調査されます．
つまり，インナークラスや匿名クラスに属するメソッドは無視されます．

##得られるメソッドの特徴
<table>
  <thead>
    <tr><th>項目</th><th>意味</th></tr>
  </thead>
  <tbody>
    <tr><td>FQMN</td><td>完全修飾メソッド名</td></tr>
    <tr><td>MN</td><td>メソッド名</td></tr>
    <tr><td>FW</td><td>メソッド名の先頭単語（レンマタイゼーション（見出し語化）は行われません）</td></tr>
    <tr><td>R-Type</td><td>戻り値型のカテゴリ（void,boolean,その他の基本型,参照型．括弧内は実際の型）</td></tr>
    <tr><td>Access</td><td>アクセス修飾子（public,private,protected,none）．</td></tr>
    <tr><td>Static</td><td>staticメソッドであるか（trueならstaticメソッド）．</td></tr>
    <tr><td>Params</td><td>仮引数．個数とそのリスト．</td></tr>
    <tr><td>throws</td><td>throws句について．例外の個数とそのリスト．</td></tr>
    <tr><td>Invoke</td><td>メソッド内で呼び出されるメソッド．個数とその名前のリスト．</td></tr>
    <tr><td>Invoke-SameName</td><td>メソッド内で呼び出されるメソッドの内，同じ名前のメソッドが呼び出される回数．</td></tr>
    <tr><td>Invoke-SameFWord</td><td>メソッド内で呼び出されるメソッドの内，同じ先頭単語を持つメソッドが呼び出される回数．</td></tr>
    <tr><td>LocalVariables</td><td>メソッドのボディーで宣言されるローカル変数の個数．</td></tr>
    <tr><td>Available V</td><td>メソッドの中で有効な各種変数の名前と，それらの具体的なスコープ．</td></tr>
    <tr><td>branch structure</td><td>分岐構造の数（ifとswitch文の個数がカウントされます）．</td></tr>
    <tr><td>loop structure</td><td>for文などの繰返し構造の数．</td></tr>
    <tr><td>return stmt</td><td>リターン文の個数</td></tr>
    <tr><td>throw stmt</td><td>throw文の個数</td></tr>
    <tr><td>try stmt</td><td>try文の個数</td></tr>
    <tr><td>cast exp</td><td>明示的キャストを行った回数</td></tr>
    <tr><td>instanceof exp</td><td>instanceof演算子の出現回数</td></tr>
    <tr><td>new stmt</td><td>new演算子によるオブジェクト生成の回数（newによる配列生成はカウントされません）．</td></tr>
    <tr><td>field Write</td><td>フィールドへの値の書込み回数．</td></tr>
    <tr><td>field Read</td><td>フィールドからの値の読出し回数</td></tr>
    <tr>
      <td>Metrics Values</td>
      <td>ソースコードメトリクス値．左からコード行数，サイクロマティック数，最大ネスト数，平均ネスト数，コメント行数（タイプ１から７）となる．</td>
    </tr>
  </tbody>
</table>
