(function () {
  var input = document.getElementById('demo-name');
  var greeting = document.getElementById('demo-greeting');
  var countEl = document.getElementById('retune-count');
  var attrsEl = document.getElementById('demo-attrs');
  var hint = document.getElementById('demo-hint');
  var node = document.querySelector('.demo-node');
  if (!input) return;

  var count = 0;
  var lastKey = null;

  function uuidStub(seed) {
    var h = (seed * 2654435761) >>> 0;
    var s = h.toString(16);
    while (s.length < 8) s = '0' + s;
    return s.slice(0, 8) + '-demo';
  }

  function retune() {
    var name = input.value.trim() || 'World';
    if (name === lastKey) return;
    lastKey = name;
    count++;
    countEl.textContent = String(count);
    greeting.textContent = 'Hello, ' + name + '!';
    node.classList.remove('flash');
    void node.offsetWidth;
    node.classList.add('flash');
    attrsEl.replaceChildren(
      attrItem('viewClass', 'AppCompatTextView'),
      attrItem('key ' + uuidStub(name.length + 7), 'text = "Hello, ' + name + '!"')
    );
    hint.textContent = count === 1
      ? '首次 Retune：建立节点树并应用全部 Attribute。'
      : '第 ' + count + ' 次 Retune：仅 text 这个 Attribute 发生变化，其余原样保留。';
  }

  function attrItem(key, value) {
    var li = document.createElement('li');
    li.textContent = key + ' → ';
    var em = document.createElement('em');
    em.textContent = value;
    li.appendChild(em);
    return li;
  }

  var timer;
  input.addEventListener('input', function () {
    clearTimeout(timer);
    timer = setTimeout(retune, 180);
  });

  var demo = document.querySelector('.demo');
  var caption = document.createElement('p');
  caption.className = 'demo-caption';
  caption.textContent = '演示为纯前端模拟：真实 Hibari 由编译器插件注入 uniqueKey、运行时按 Attribute 比对新旧节点树后更新 View。';
  demo.appendChild(caption);
})();
