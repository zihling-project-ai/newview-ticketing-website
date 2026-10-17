/* Shared social-page controller; content is rendered with textContent, never HTML interpolation. */
'use strict';
(() => {
  const $ = id => document.getElementById(id);
  const path = location.pathname.split('/').pop();
  const params = new URLSearchParams(location.search);
  let session = {},
    categories = [],
    pictures = [],
    cartTimer;

  function message(text, error = false) {
    const status = $('page-status');
    status.replaceChildren();
    if (text) {
      node('span', text, status);
      const close = node('button', '×', status);
      close.type = 'button';
      close.setAttribute('aria-label', '關閉通知');
      close.onclick = () => message('');
    }
    status.hidden = !String(text || '').trim();
    status.style.color = error ? '#9f3027' : '';

  }
  async function api(url, data) {
    const options = {
      credentials: 'same-origin'
    };
    if (data !== undefined) {
      options.method = 'POST';
      options.headers = {
        'X-CSRF-Token': session.csrf || ''
      };
      options.body = data instanceof FormData ? data : new URLSearchParams(data);
    }
    let response;try{response=await fetch(url,options);}catch{const error=new Error("連線中斷，請稍後重試；若剛送出結帳，請先確認訂單紀錄。");error.status=0;throw error;}
    const text = await response.text();
    let body;
    try {
      body = JSON.parse(text);
    } catch {
      body = text;
    }
    if (!response.ok) {
      const hints = {
        400: '資料格式有誤，請檢查輸入或圖片。',
        401: '請先登入再操作。',
        403: '沒有操作權限，或登入已過期，請重新整理。',
        404: '找不到這篇文章或資料。',
        413: '圖片總大小超過限制。'
      };
      const error=new Error(body.error||hints[response.status]||'操作未完成，請稍後重試。');error.status=response.status;throw error;
    }
    return body;
  }

  function node(tag, text, parent, className) {
    const e = document.createElement(tag);
    if (text !== undefined) e.textContent = text;
    if (className) e.className = className;
    if (parent) parent.append(e);
    return e;
  }

  function action(label, parent, run, cls = '') {
    const e = node('button', label, parent, cls);
    e.type = 'button';
    e.onclick = () => Promise.resolve().then(run).catch(error => message(error.message, true));
    return e;
  }

  function detailUrl(p) {
    return 'forum_postdetail.html?postID=' + p.postID;
  }

  function loginUrl() {
    return 'sign-in.html?next=' + encodeURIComponent(path + location.search);
  }

  function safeNext() {
    const value = params.get('next') || 'forum_home.html';
    const url = new URL(value, location.href);
    const allowed = ['my-coupons.html', 'tickets.html', 'ticket.html', 'redeem-ticket.html', 'seats.html', 'checkout_cart.html', 'checkout_thankupage.html', 'activities.html', 'activity-detail.html', 'product.html', 'product-detail.html', 'myOrder.html', 'my-favourite.html', 'change-password.html', 'member.html', 'my-profile.html', 'forum_home.html', 'forum_post.html', 'forum_dopost.html', 'forum_postdetail.html', 'forum_revisedpost.html'];
    return url.origin === location.origin && url.pathname.startsWith(location.pathname.slice(0, location.pathname.lastIndexOf('/') + 1)) && allowed.includes(url.pathname.split('/').pop()) ? url.pathname + url.search : 'forum_home.html';
  }

  function date(value) {
    return String(value || '').replace('T', ' ').replace(/\.\d+$/, '').slice(0, 16);
  }

  function image(url, alt, parent) {
    const img = node('img', undefined, parent);
    img.src = url || 'img/nopic.jpg';
    img.alt = alt;
    img.loading = 'lazy';
    return img;
  }

  function card(post, parent) {
    const article = node('article', undefined, parent, 'nv-card nv-social-card');
    const media = node('a', undefined, article, 'nv-card-media');
    media.href = detailUrl(post);
    if (post.postPic[0]) image(post.postPic[0], post.postHeader, media);
    else node('div', 'NewView', media, 'nv-placeholder');
    const body = node('div', undefined, article, 'nv-card-body');
    node('span', post.postCategoryName, body, 'nv-tag');
    const heading = node('h3', undefined, body);
    const link = node('a', post.postHeader, heading);
    link.href = detailUrl(post);
    node('p', post.postContent, body);
    node('small', post.userName + ' · ' + date(post.postDateTime), body);
    const stats = node('div', undefined, body, 'nv-card-stats');
    node('span', '讚 ' + post.likeCount, stats);
    node('span', '倒讚 ' + post.dislikeCount, stats);
    node('span', '留言 ' + post.postMessageIDs.length, stats);
  }
  async function home() {
    const [posts, popular] = await Promise.all([api('GetPostServlet?action=getAll'), api('GetPostServlet?action=getbylike')]);
    $('latest-posts').replaceChildren();
    $('popular-posts').replaceChildren();
    posts.slice(0, 6).forEach(p => card(p, $('latest-posts')));
    popular.forEach(p => card(p, $('popular-posts')));
    if (!posts.length) node('p', '目前沒有文章，來分享第一篇吧。', $('latest-posts'), 'nv-empty');
    if (!popular.length) node('p', '目前沒有熱門文章。', $('popular-posts'), 'nv-empty');
    const tabs = $('category-tabs');
    const table = {
      category: Number(params.get('postCategoryID')) || 0,
      query: '',
      size: 10,
      page: 1,
      sort: 'author',
      direction: 1
    };
    const textCompare = new Intl.Collator('zh-Hant', {
      numeric: true,
      sensitivity: 'base'
    });
    const values = p => ({
      author: p.userName || '',
      title: p.postHeader || '',
      date: date(p.postDateTime),
      comments: p.postMessageIDs.length,
      hot: Number(p.likeCount) + Number(p.dislikeCount)
    });

    function renderTable() {
      tabs.querySelectorAll('button').forEach(b => b.setAttribute('aria-pressed', String(Number(b.dataset.category) === table.category)));
      const base = posts.filter(p => !table.category || p.postCategoryID === table.category);
      const words = table.query.trim().normalize('NFKC').toLocaleLowerCase('zh-Hant').split(/\s+/).filter(Boolean);
      const filtered = base.filter(p => {
        const searchable = Object.values(values(p)).join(' ').normalize('NFKC').toLocaleLowerCase('zh-Hant');
        return words.every(word => searchable.includes(word));
      });
      filtered.sort((a, b) => {
        const left = values(a)[table.sort],
          right = values(b)[table.sort];
        const compared = typeof left === 'number' ? left - right : textCompare.compare(left, right);
        return compared * table.direction || a.postID - b.postID;
      });
      const pages = Math.max(1, Math.ceil(filtered.length / table.size));
      table.page = Math.max(1, Math.min(table.page, pages));
      const start = (table.page - 1) * table.size;
      const tbody = $('category-posts');
      tbody.replaceChildren();
      filtered.slice(start, start + table.size).forEach(p => {
        const row = node('tr', undefined, tbody),
          data = values(p);
        node('td', data.author, row);
        const title = node('td', undefined, row),
          link = node('a', data.title, title);
        link.href = detailUrl(p);
        node('td', data.date, row);
        node('td', data.comments, row);
        node('td', data.hot, row);
      });
      if (!filtered.length) {
        const cell = node('td', table.query.trim() ? '沒有符合的結果。' : '這個分類還沒有文章。', node('tr', undefined, tbody));
        cell.colSpan = 5;
      }
      $('table-info').textContent = '顯示第 ' + (filtered.length ? start + 1 : 0) + ' 至 ' + Math.min(start + table.size, filtered.length) + ' 筆結果，共 ' + filtered.length + ' 筆' + (filtered.length !== base.length ? '（從 ' + base.length + ' 筆結果中過濾）' : '');
      $('table-page').textContent = '第 ' + table.page + ' / ' + pages + ' 頁';
      $('table-first').disabled = $('table-prev').disabled = table.page === 1;
      $('table-next').disabled = $('table-last').disabled = table.page === pages;
      document.querySelectorAll('[data-sort]').forEach(button => {
        const active = button.dataset.sort === table.sort;
        button.closest('th').setAttribute('aria-sort', active ? (table.direction === 1 ? 'ascending' : 'descending') : 'none');
        button.querySelector('span').textContent = active ? (table.direction === 1 ? ' ↑' : ' ↓') : ' ↕';
      });
    }
    for (const c of [{
        postCategoryID: 0,
        postCategoryName: '全部文章'
      }, ...categories]) {
      const button = action(c.postCategoryName, tabs, () => {
        table.category = c.postCategoryID;
        table.page = 1;
        renderTable();
      });
      button.dataset.category = c.postCategoryID;
    }
    $('table-search').oninput = e => {
      table.query = e.target.value;
      table.page = 1;
      renderTable();
    };
    $('table-size').onchange = e => {
      table.size = Number(e.target.value);
      table.page = 1;
      renderTable();
    };
    document.querySelectorAll('[data-sort]').forEach(button => button.onclick = () => {
      table.direction = table.sort === button.dataset.sort ? -table.direction : 1;
      table.sort = button.dataset.sort;
      table.page = 1;
      renderTable();
    });
    $('table-first').onclick = () => {
      table.page = 1;
      renderTable();
    };
    $('table-prev').onclick = () => {
      table.page--;
      renderTable();
    };
    $('table-next').onclick = () => {
      table.page++;
      renderTable();
    };
    $('table-last').onclick = () => {
      table.page = Math.max(1, Math.ceil(posts.length / table.size));
      renderTable();
    };
    renderTable();
  }

  function renderPreviews() {
    const area = $('image-previews');
    area.replaceChildren();
    pictures.forEach((picture, index) => {
      const box = node('div', undefined, area, 'nv-preview');
      image(picture.url, '文章圖片 ' + (index + 1), box);
      action('移除圖片 ' + (index + 1), box, () => {
        if (picture.file) URL.revokeObjectURL(picture.url);
        pictures.splice(index, 1);
        renderPreviews();
      });
    });
  }

  function addFiles(files) {
    if (pictures.length + files.length > 3) {
      message('每篇最多 3 張圖片；請先移除不需要的圖片。', true);
      return;
    }
    for (const file of files) {
      if (!['image/png', 'image/jpeg'].includes(file.type) || !file.size || file.size > 2 * 1024 * 1024) {
        message('請選擇每張不超過 2 MB 的 PNG 或 JPEG。', true);
        return;
      }
    }
    files.forEach(file => pictures.push({
      file,
      url: URL.createObjectURL(file)
    }));
    renderPreviews();
    message('圖片已加入預覽，儲存文章後才會上傳。');
  }
  async function editor() {
    if (!session.userID) {
      location.href = loginUrl();
      return;
    }
    const editing = path === 'forum_revisedpost.html';
    const select = $('postcategory');
    categories.forEach(c => {
      const o = node('option', c.postCategoryName, select);
      o.value = c.postCategoryID;
    });
    if (editing) {
      const post = await api('GetPostDetailServlet?postID=' + encodeURIComponent(params.get('postID') || ''));
      if (String(post.userID) !== String(session.userID)) {
        $('postform').hidden = true;
        throw new Error('只有作者可以修改文章。');
      }
      $('posttitle').value = post.postHeader;
      $('postcontent').value = post.postContent;
      select.value = post.postCategoryID;
      pictures = post.images.map(p => ({
        id: p.id,
        url: p.url
      }));
      renderPreviews();
    }
    const updateCounts=()=>{ $('post-title-count').textContent=$('posttitle').value.length+' / 100';$('post-content-count').textContent=$('postcontent').value.length+' / 10000';};
    $('posttitle').addEventListener('input',updateCounts);$('postcontent').addEventListener('input',updateCounts);updateCounts();
    $('postimages').onchange = e => {
      addFiles([...e.target.files]);
      e.target.value = '';
    };
    const drop = document.querySelector('.nv-drop');
    drop.ondragover = e => e.preventDefault();
    drop.ondrop = e => {
      e.preventDefault();
      addFiles([...e.dataTransfer.files]);
    };
    $('postform').onsubmit = async e => {
      e.preventDefault();
      const button = $('submitpost');
      button.disabled = true;
      try {
        const data = new FormData(e.target);
        data.delete('images');
        data.append('action', editing ? 'update' : 'add');
        if (editing) data.append('postID', params.get('postID'));
        data.append('replaceImages', 'true');
        data.append('keepImageIds', JSON.stringify(pictures.filter(p => p.id).map(p => p.id)));
        pictures.filter(p => p.file).forEach(p => data.append('images', p.file));
        const result = await api('DoPostServlet', data);
        location.href = detailUrl(result);
      } catch (error) {
        message(error.message, true);
        button.disabled = false;
      }
    };
  }
  async function comments(postID) {
    const items = await api('DoMessageServlet?action=GetAll&postID=' + postID);
    $('messageCount').textContent = items.length;
    const area = $('commentsContainer');
    area.replaceChildren();
    if (!items.length) node('p', '還沒有留言，來聊聊你的想法。', area, 'nv-empty');
    for (const item of items) {
      const box = node('article', undefined, area, 'nv-comment');
      node('strong', item.userName || item.userAccount, box);
      node('time', date(item.messageDate), box);
      node('p', item.mesContent, box);
      if (String(item.userID) === String(session.userID)) {
        action('修改留言', box, async () => {
          if (box.querySelector('textarea')) return;
          const edit = node('textarea', undefined, box, 'form-control');
          edit.value = item.mesContent;
          edit.maxLength = 1000;
          edit.setAttribute('aria-label', '修改留言內容');
          action('儲存留言', box, async () => {
            if (!edit.value.trim()) throw new Error('請輸入留言。');
            await api('DoMessageServlet', {
              action: 'Update',
              postMessageID: item.postMessageID,
              mesContent: edit.value
            });
            await comments(postID);
            message('留言已更新。');
          });
        });
        action('刪除留言', box, async () => {
          if (confirm('確定刪除這則留言？')) {
            await api('DoMessageServlet', {
              action: 'Delete',
              postMessageID: item.postMessageID
            });
            await comments(postID);
            message('留言已刪除。');
          }
        });
      }
    }
  }
  async function detail() {
    const id = Number(params.get('postID'));
    if (!Number.isInteger(id) || id <= 0) throw new Error('文章編號無效。');
    const p = await api('GetPostDetailServlet?postID=' + id);
    document.title = p.postHeader + ' · NewView';
    $('postHeader').textContent = p.postHeader;
    $('postContent').textContent = p.postContent;
    $('cate').textContent = p.categoryName;
    $('name').textContent = p.userName;
    $('post-author-avatar').textContent=[...(p.userName||'N')][0];
    $('day2').textContent = date(p.postDateTime);
    $('edited-date').textContent = ' · 更新 ' + date(p.lastEditedTime);
    $('likeCount').textContent = p.likeCount;
    $('dislikeCount').textContent = p.dislikeCount;
    const album = document.querySelector('#myCarousel .carousel-inner');
    p.images.forEach((picture, i) => {
      const slide = node('div', undefined, album, 'carousel-item' + (i ? '' : ' active'));
      const img = image(picture.url, '文章圖片 ' + (i + 1), slide);
      img.loading = 'eager';
    });
    $('picturearea').hidden = !p.images.length;
    $('image-caption').textContent = p.images.length + ' 張文章圖片';
    document.querySelectorAll('#myCarousel .carousel-control-prev,#myCarousel .carousel-control-next').forEach(e => e.hidden = p.images.length < 2);
    const own = String(p.userID) === String(session.userID);
    $('author-actions').hidden = !own;
    $('modifyButton').href = 'forum_revisedpost.html?postID=' + id;
    $('deleteButton').onclick = async () => {
      try {
        if (confirm('刪除此文章與相關留言、圖片？')) {
          await api('DeletePostServlet', {
            postID: id
          });
          location.href = 'forum_home.html';
        }
      } catch (e) {
        message(e.message, true);
      }
    };
    for (const type of ['like', 'dislike']) $(type + 'Button').onclick = async () => {
      try {
        if (!session.userID) {
          location.href = loginUrl();
          return;
        }
        const result = await api('DoLikeDislikeServlet', {
          action: type,
          postID: id
        });
        $('likeCount').textContent = result.likeCount;
        $('dislikeCount').textContent = result.dislikeCount;
        message(type === 'like' ? '已按讚。' : '已記錄倒讚。');
      } catch (e) {
        message(e.message, true);
      }
    };
    $('comment-form').hidden = !session.userID;
    $('login-prompt').hidden = !!session.userID;
    $('comment-login').href = loginUrl();
    $('comment-form').onsubmit = async e => {
      e.preventDefault();
      try {
        const content = $('mesContent').value.trim();
        if (!content) return;
        await api('DoMessageServlet', {
          action: 'AddMsg',
          postID: id,
          MesContent: content
        });
        $('mesContent').value = '';
        await comments(id);
        message('留言已送出。');
      } catch (error) {
        message(error.message, true);
      }
    };
    await comments(id);
  }

  function setupHero() {
    const element = $('hero-carousel');
    if (!element) return;
    const slides = [...element.querySelectorAll('.nv-hero-slide')];
    const track = element.querySelector('.nv-hero-track');
    const indicators = [...element.querySelectorAll('[data-hero-index]')];
    const toggle = $('hero-toggle');
    let index = 0,
      timer = null,
      playing = !window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    function pauseTimer() {
      window.clearInterval(timer);
      timer = null;
    }

    function startTimer() {
      pauseTimer();
      if (playing) timer = window.setInterval(() => show(index + 1), 5000);
    }

    function show(next) {
      index = (next + slides.length) % slides.length;
      track.style.transform = 'translateX(-' + index * 100 + '%)';
      slides.forEach((slide, i) => {
        slide.classList.toggle('is-active', i === index);
        slide.inert = i !== index;
        slide.setAttribute('aria-hidden', String(i !== index));
      });
      indicators.forEach((button, i) => button.setAttribute('aria-current', String(i === index)));
    }
    indicators.forEach((button, i) => button.onclick = () => {
      show(i);
      startTimer();
    });
    element.querySelectorAll('[data-hero-dir]').forEach(button => button.onclick = e => {
      e.preventDefault();
      show(index + Number(button.dataset.heroDir));
      startTimer();
    });
    toggle.onclick = () => {
      playing = !playing;
      toggle.textContent = playing ? '暫停輪播' : '播放輪播';
      startTimer();
    };
    element.addEventListener('mouseenter', pauseTimer);
    element.addEventListener('mouseleave', startTimer);
    element.addEventListener('focusin', pauseTimer);
    element.addEventListener('focusout', e => {
      if (!element.contains(e.relatedTarget)) startTimer();
    });
    toggle.textContent = playing ? '暫停輪播' : '播放輪播';
    show(0);
    startTimer();
  }
  const money = value => value == null ? '未提供' : 'NT$ ' + Number(value).toLocaleString('zh-TW');
  const orderStatus = value => ({
    0: '未使用',
    1: '已使用',
    2: '已取消'
  })[value] || ('狀態代碼 ' + (value ?? '未提供'));
  function orderContents(order, parent) {
    const groups=new Map();
    for(const item of order.items || []){
      const key=String(item.actID);
      if(!groups.has(key))groups.set(key,[]);
      groups.get(key).push(item);
    }
    const area=node('div',undefined,parent,'nv-order-activities');
    if(!groups.size)node('p','此訂單目前沒有可顯示的活動明細。',area,'nv-muted');
    for(const items of groups.values()){
      const first=items[0],group=node('section',undefined,area,'nv-order-activity');
      node('h3',first.actName||'原活動資料未提供',group);
      node('p',[date(first.actDate),String(first.time||'').slice(0,5),first.venue].filter(Boolean).join(' · ')||'活動時間或場地未提供',group,'nv-order-event-meta');
      const seats=items.map(i=>i.seat).filter(Boolean);
      node('p',items.length+' 張票券 · '+(seats.length?'座位 '+seats.join('、'):'未指定座位'),group,'nv-order-event-seats');
      const links=node('div',undefined,group,'nv-order-ticket-links');
      for(const item of items)node('a','票券 #'+item.orderListID+(item.seat?' · '+item.seat:''),links).href='ticket.html?ticketID='+item.orderListID;
    }
  }
  async function memberOrders() {
    let orders = await api('MemberOrders');
    const requested=params.get('orderID');
    if(requested){if(!/^[1-9][0-9]*$/.test(requested))throw new Error('訂單編號不正確。');orders=orders.filter(o=>String(o.orderID)===requested);if(!orders.length)throw new Error('找不到這筆訂單。');}
    const host = $('member-orders');
    host.replaceChildren();
    if(requested)node('a','← 查看全部訂單',host,'nv-order-back-link').href='myOrder.html';
    if (!orders.length) node('p', '目前沒有訂單。', host, 'nv-empty');
    for (const order of orders) {
      const box = node('article', undefined, host, 'nv-form-card nv-member-order');
      node('h2', '訂單 #' + order.orderID, box);
      const state=Number(order.simulation)===1?(Number(order.ordType)===2?'已取消':Number(order.ordType)===1?'已使用':Number(order.usedCount)>0?'部分票券已使用':'已成立，待使用'):orderStatus(order.ordType);
      node('span',state,box,'nv-member-status'+(Number(order.ordType)===2?' is-muted':''));
      node('p',date(order.ordTime)+' · '+(order.publisher || '活動主辦'),box,'nv-member-order-meta');
      orderContents(order,box);
      const amounts=node('dl',undefined,box,'nv-member-order-amounts');
      for(const [label,value] of [['票券數量',(order.actQuantity??0)+' 張'],['原價',money(order.ordTotal)],['優惠折抵',money(order.discount||0)],['訂單金額',money(Number(order.ordTotal)-Number(order.discount||0))]]){const part=node('div',undefined,amounts);node('dt',label,part);node('dd',value,part);}
      node('a','查看訂單 #'+order.orderID+' 的票券',box,'nv-button').href='tickets.html?orderID='+order.orderID;
      if (Number(order.simulation) === 1 && Number(order.ordType) === 3 && Number(order.usedCount) === 0) {
        const cancel = action('取消訂單 #' + order.orderID, box, () => {
          cancel.hidden = true;
          const confirm = node('div', undefined, box);
          node('p', '取消後將釋放座位並歸還本筆使用的優惠券；已過期的券仍不能用。不會進行付款或退款。確定取消？', confirm);
          const yes = action('確認取消 #' + order.orderID, confirm, async () => {
            yes.disabled = true;
            try {
              await api('CancelLocalOrder', {
                orderID: order.orderID,
                confirmCancellation: 'true'
              });
              $('order-detail').hidden = true;
              await memberOrders();
              message('模擬訂單已取消，已釋放座位並歸還本筆優惠券（如有使用）；未進行退款。');
            } catch (error) {
              yes.disabled = false;
              throw error;
            }
          }, 'nv-button');
          action('保留訂單', confirm, () => {
            confirm.remove();
            cancel.hidden = false;
          }, 'nv-button nv-button-outline');
        }, 'nv-button nv-button-outline');
      }
      if (Number(order.usedCount) > 0 && Number(order.ordType) !== 2) node('p', '已使用 ' + order.usedCount + ' 張票券，此訂單無法取消。', box, 'nv-muted');
      if (order.cancelledAt) node('p', '取消時間：' + date(order.cancelledAt), box, 'nv-muted');
      action('查看訂單 #' + order.orderID + ' 明細', box, async () => {
        const items = await api('MemberOrders?orderID=' + order.orderID);
        $('order-detail-title').textContent = '訂單 #' + order.orderID + ' 明細';
        $('order-detail').hidden = false;
        $('order-items').replaceChildren();
        if (!items.length) node('p', '這筆訂單沒有票券明細。', $('order-items'), 'nv-empty');
        for (const item of items) {
          const row = node('article', undefined, $('order-items'), 'nv-form-card');
          node('h3', item.actName || '原活動資料未提供', row);
          node('p', '票券 #' + item.orderListID + ' · 活動日期：' + date(item.actDate) + ' · 明細金額：' + money(item.actTotal), row);
          node('p', '使用狀態：' + (Number(order.ordType) === 2 ? '已取消' : (({
            0: '未使用',
            1: '已使用'
          })[item.usedType] || '未提供')) + (item.seat == null ? '' : ' · 原座位代碼：' + item.seat), row);
          node('a', '查看票券 #' + item.orderListID, row, 'nv-button nv-button-outline').href = 'ticket.html?ticketID=' + item.orderListID;
        }
      }, 'nv-button nv-button-outline');
    }
  }
  async function memberFavorites() {
    const activities = await api('MemberActivities');
    const renderCard = (activity, parent, saved) => {
      const card = node('article', undefined, parent, 'nv-card');
      if (Number(activity.hasImage) === 1) {
        const img = image('MemberActivityImage?actID=' + activity.actID, activity.actName || '活動圖片', node('div', undefined, card, 'nv-card-media'));
        img.onerror = () => {
          img.hidden = true;
        };
      } else node('div', 'NewView', card, 'nv-placeholder');
      const body = node('div', undefined, card, 'nv-card-body');
      const title = node('h3', undefined, body);
      node('a', activity.actName || '未命名活動', title).href = 'activity-detail.html?actID=' + activity.actID;
      node('p', activity.actIntroduce || '尚無活動介紹', body);
      node('small', date(activity.actDate) + ' · ' + money(activity.actPrice), body);
      node('small', activity.cityAddress || '場地未提供', body);
      const button = action(saved ? '取消收藏' : '加入收藏', body, async () => {
        button.disabled = true;
        try {
          await api('MemberFavorites', {
            action: saved ? 'remove' : 'add',
            actID: activity.actID
          });
          await memberFavorites();
          $('favorite-result').textContent = saved ? '已取消收藏。' : '已加入收藏。';
          message('');
        } finally {
          button.disabled = false;
        }
      }, 'nv-button nv-button-outline');
    };
    const favorites = await api('MemberFavorites');
    $('member-favorites').replaceChildren();
    if (!favorites.length) node('p', '目前沒有收藏活動。', $('member-favorites'), 'nv-empty');
    for (const activity of favorites) renderCard(activity, $('member-favorites'), true);
    const renderActivities = () => {
      const query = $('activity-search').value.trim().toLocaleLowerCase();
      const matching = activities.filter(a => ((a.actName || '') + ' ' + (a.actIntroduce || '')).toLocaleLowerCase().includes(query));
      $('member-activities').replaceChildren();
      if (!matching.length) node('p', '沒有符合的活動。', $('member-activities'), 'nv-empty');
      for (const a of matching) renderCard(a, $('member-activities'), Number(a.favorite) === 1);
    };
    $('activity-search').oninput = renderActivities;
    renderActivities();
  }
  async function favoriteActivity(activity, button) {
    if (!session.userID) {
      location.href = loginUrl();
      return;
    }
    button.disabled = true;
    try {
      const result = await api('MemberFavorites', {
        action: Number(activity.favorite) === 1 ? 'remove' : 'add',
        actID: activity.actID
      });
      activity.favorite = result.favorite ? 1 : 0;
      button.textContent = activity.favorite ? '取消收藏' : '加入收藏';
      button.setAttribute('aria-pressed', String(!!activity.favorite));
      message(activity.favorite ? '已加入收藏。' : '已取消收藏。');
    } finally {
      button.disabled = false;
    }
  }
  async function activityCatalog() {
    const activities = await api('ActivityList');
    let page = 1;
    const size = 12;
    for (const [key, label, target] of [
        ['categoryID', 'categoryName', 'catalog-category'],
        ['cityID', 'cityName', 'catalog-city']
      ]) {
      const choices = new Map();
      for (const a of activities)
        if (a[key] != null && a[label]) choices.set(String(a[key]), a[label]);
      for (const [id, name] of choices) node('option', name, $(target)).value = id;
    }
    const render = () => {
      const query = $('catalog-search').value.trim().normalize('NFKC').toLowerCase();
      const category = $('catalog-category').value,
        city = $('catalog-city').value;
      const status=$('catalog-status').value;
      const items = activities.filter(a => (!status || (status==='ended'?Number(a.ended)===1:Number(a.ended)!==1)) && (!category || String(a.categoryID) === category) && (!city || String(a.cityID) === city) && ((a.actName || '') + ' ' + (a.actIntroduce || '') + ' ' + (a.cityAddress || '')).normalize('NFKC').toLowerCase().includes(query));
      const sort = $('catalog-sort').value;
      items.sort((a, b) => {
        if (sort.startsWith('price')) {
          if (a.actPrice == null && b.actPrice != null) return 1;
          if (b.actPrice == null && a.actPrice != null) return -1;
          return ((a.actPrice ?? 0) - (b.actPrice ?? 0)) * (sort === 'price-desc' ? -1 : 1) || a.actID - b.actID;
        }
        if(Number(a.ended)!==Number(b.ended))return Number(a.ended)-Number(b.ended);
        return String(a.actDate || '9999').localeCompare(String(b.actDate || '9999')) || a.actID - b.actID;
      });
      const pages = Math.max(1, Math.ceil(items.length / size));
      page = Math.min(page, pages);
      $('catalog-count').textContent = '共 ' + items.length + ' 個活動';
      $('catalog-page').textContent = page + ' / ' + pages;
      $('catalog-prev').disabled = page === 1;
      $('catalog-next').disabled = page === pages;
      $('activity-catalog').replaceChildren();
      if (!items.length) node('p', '沒有符合條件的活動。', $('activity-catalog'), 'nv-empty');
      for (const a of items.slice((page - 1) * size, page * size)) {
        const card = node('article', undefined, $('activity-catalog'), 'nv-card nv-activity-card');
        card.classList.toggle('is-ended',Number(a.ended)===1);
        const media = node('a', undefined, card, 'nv-card-media');
        media.href = 'activity-detail.html?actID=' + a.actID;
        if (Number(a.hasImage)) {
          const img = image('ActivityImage?actID=' + a.actID, a.actName || '活動圖片', media);
          img.onerror = () => {
            img.hidden = true;
            node('span', '暫無可用圖片', media);
          };
        } else node('div', 'NewView', media, 'nv-placeholder');
        const body = node('div', undefined, card, 'nv-card-body');
        const badges=node('div',undefined,body,'nv-activity-card-badges');
        node('span',a.categoryName || '未分類',badges,'nv-tag');
        node('span',Number(a.ended)===1?'已結束':Number(a.seatMode)===1?'指定座位':'非指定座位',badges,'nv-activity-card-mode');
        node('a', a.actName || '未命名活動', node('h3', undefined, body)).href = media.href;
        node('p', a.actIntroduce || '尚無活動介紹', body,'nv-activity-card-intro');
        node('small',date(a.actDate) || '日期未提供',body,'nv-activity-card-date');
        node('small',[a.cityName,a.cityAddress].filter(Boolean).join(' · ') || '場地未提供',body,'nv-activity-card-venue');
        const footer=node('div',undefined,body,'nv-activity-card-footer');
        node('strong',money(a.actPrice),footer,'nv-activity-card-price');
        const button = action(session.userID ? (Number(a.favorite) === 1 ? '取消收藏' : '加入收藏') : '登入後收藏', footer, () => favoriteActivity(a, button), 'nv-activity-favorite');
        button.setAttribute('aria-pressed', String(Number(a.favorite) === 1));
        const add = action(Number(a.ended) === 1 ? '活動已結束' : Number(a.seatMode) === 1 ? '選位並加入購物車' : '加入購物車', body, () => addActivityToCart(a, 1, add), 'nv-button nv-activity-card-buy');
        add.disabled = Number(a.ended) === 1;
      }
    };
    for (const id of ['catalog-search', 'catalog-category', 'catalog-city', 'catalog-sort', 'catalog-status']) $(id)[id === 'catalog-search' ? 'oninput' : 'onchange'] = () => {
      page = 1;
      render();
    };
    $('catalog-prev').onclick = () => {
      page--;
      render();
    };
    $('catalog-next').onclick = () => {
      page++;
      render();
    };
    render();
  }
  async function activityDetail() {
    const id = params.get('actID');
    if (!id || !/^\d+$/.test(id) || Number(id) <= 0) {
      message('找不到活動，請從活動列表選擇。', true);
      return;
    }
    const a = await api('ActivityDetail?actID=' + encodeURIComponent(id));
    await activityReviews(a.actID);
    $('activity-detail').hidden = false;
    $('activity-name').textContent = a.actName || '未命名活動';
    $('activity-category').textContent = a.categoryName || '未分類';
    $('activity-summary').textContent = a.actIntroduce || '';
    $('activity-content').textContent = a.actContent || a.actIntroduce || '尚無活動介紹';
    $('activity-date').textContent=(date(a.actDate)||'日期未提供')+' '+String(a.time||'').slice(0,5);
    $('activity-venue').textContent=[a.cityName,a.cityAddress].filter(Boolean).join(' · ')||'場地未提供';
    $('activity-publisher').textContent=a.publisher||'未提供';
    $('activity-seat-label').textContent=Number(a.seatMode)===1?'指定座位':'非指定座位';
    $('activity-reservation-note').textContent=Number(a.ended)===1?'活動已結束':Number(a.seatMode)===1?'指定座位保留 10 分鐘':'非指定座位，無須保留座位';
    $('activity-state').textContent=Number(a.ended)===1?'活動已結束':'期待與你相遇';
    $('activity-unit-price').textContent=money(a.actPrice);
    const quantity=$('cart-quantity');
    const updateAmount=()=>{$('activity-total').textContent=money(Number(a.actPrice)*Number(quantity.value||1));};
    quantity.oninput=updateAmount;updateAmount();
    $('activity-quantity-wrap').hidden=Number(a.seatMode)===1||Number(a.ended)===1;
    $('activity-meta').textContent = (date(a.actDate) || '日期未提供') + ' ' + (a.time || '') + ' · ' + money(a.actPrice) + ' · ' + (a.cityAddress || '場地未提供') + ' · 主辦：' + (a.publisher || '未提供');
    $('activity-seat-mode').textContent = Number(a.seatMode) === 1 ? '指定座位｜可先查看座位圖，登入後保留座位。' : '非指定座位｜本活動無須選位，入座方式請見活動介紹。';
    const add = $('activity-cart');
    if (Number(a.ended) === 1) {
      add.textContent = '活動已結束';
      add.disabled = true;
    }
    if (Number(a.seatMode) === 1 && !Number(a.ended)) {
      add.textContent = '選位並加入購物車';
      $('cart-quantity').hidden = true;
      document.querySelector('label[for=cart-quantity]').hidden = true;
    }
    add.onclick = () => {if(Number(a.seatMode)!==1&&!quantity.reportValidity())return;addActivityToCart(a,quantity.value,add).catch(e=>message(e.message,true));};
    const button = $('activity-favorite');
    button.textContent = session.userID ? (Number(a.favorite) === 1 ? '取消收藏' : '加入收藏') : '登入後收藏';
    button.setAttribute('aria-pressed', String(Number(a.favorite) === 1));
    button.onclick = () => favoriteActivity(a, button).catch(e => message(e.message, true));
    if (!a.imageIDs.length) node('p', '此活動暫無圖片。', $('activity-images'), 'nv-empty');
    for (const pic of a.imageIDs) {
      const img = image('ActivityImage?actID=' + a.actID + '&picID=' + pic, a.actName || '活動圖片', $('activity-images'));
      img.style.width = '100%';
      img.style.objectFit = 'contain';
      img.onerror = () => {
        img.hidden = true;
        node('p', '此張圖片暫時無法顯示。', $('activity-images'), 'nv-empty');
      };
    }
  }
  async function addActivityToCart(activity, quantity, button) {
    if (Number(activity.seatMode) === 1) {
      location.href = 'seats.html?actID=' + activity.actID;
      return;
    }
    if (!session.userID) {
      location.href = loginUrl();
      return;
    }
    button.disabled = true;
    try {
      await api('Cart', {
        action: 'add',
        actID: activity.actID,
        quantity
      });
      message('已加入購物車，可至購物車進行模擬結帳。');
    } finally {
      button.disabled = false;
    }
  }
  async function memberCoupons() {
    const coupons = await api('MemberCoupons');
    $('member-coupons').replaceChildren();
    if (!coupons.length) node('p', '目前沒有會員優惠券。', $('member-coupons'), 'nv-empty');
    for (const c of coupons) {
      const card = node('article', undefined, $('member-coupons'), 'nv-form-card nv-member-coupon'+(!c.available?' is-inactive':''));
      node('span',c.status,card,'nv-member-status'+(!c.available?' is-muted':''));
      node('strong',money(c.amount),card,'nv-coupon-value');
      node('h2', c.content || '會員優惠券', card);
      node('p', '適用：' + (c.publisher || '全站（限單一廠商結帳）'), card);
      node('p', '期限：' + date(c.startsAt) + ' 至 ' + date(c.endsAt) + '（不含截止時間）', card);
    }
  }
  let cartScope, cartCoupon = '0',
    cartBusy = false;

  function freezeCart() {
    const controls = [...document.querySelectorAll('main input,main select,main button')].map(e => [e, e.disabled]);
    controls.forEach(([e]) => e.disabled = true);
    return () => controls.forEach(([e, disabled]) => {
      if (e.isConnected && e.id !== 'checkout-submit') e.disabled = disabled;
    });
  }
  async function cartMutation(data, nextScope, notice) {
    if (cartBusy) return;
    cartBusy = true;
    const unlock = freezeCart();
    let error;
    try {
      await api('Cart', data);
    } catch (e) {
      error = e;
    } finally {
      unlock();
      cartBusy = false;
    }
    await cartPage(cartCoupon, nextScope);
    if (error) message(error.message, true);
    else if (!$('page-status').textContent) message(notice);
  }
  async function cartPage(selected = cartCoupon, selection = cartScope) {
    if (cartBusy) return;
    cartBusy = true;
    const unlock = freezeCart();
    if (cartTimer) clearInterval(cartTimer);
    $('checkout-submit').disabled = true;
    try {
      const query = selection === undefined ? '' : '&actIDs=' + encodeURIComponent(selection.join(','));
      let cart, notice = '';
      try {
        cart = await api('Cart?useDisID=' + encodeURIComponent(selected) + query);
      } catch (error) {
        if(String(selected)==='0'||![404,409].includes(error.status))throw error;
        cart = await api('Cart?useDisID=0' + query);
        notice = '原優惠券目前不適用，已取消折抵。' + error.message;
      }
      const coupons = await api('MemberCoupons');
      cartScope = cart.actIDs ? cart.actIDs.split(',').map(Number) : [];
      cartCoupon = String(cart.coupon.useDisID);
      $('cart-items').replaceChildren();
      const choices = $('cart-coupon');
      choices.replaceChildren();
      node('option', '不使用優惠券', choices).value = '0';
      for (const c of coupons) {
        const option = node('option', (c.content || '優惠券') + ' · ' + money(c.amount) + ' · ' + c.status, choices);
        option.value = c.useDisID;
        option.disabled = !c.available;
      }
      choices.value = cartCoupon;
      choices.onchange = () => cartPage(choices.value, cartScope).catch(e => message(e.message, true));
      const all = $('cart-select-all');
      all.checked = cart.items.length > 0 && cartScope.length === cart.items.length;
      all.indeterminate = cartScope.length > 0 && cartScope.length < cart.items.length;
      all.onchange = () => cartPage(cartCoupon, all.checked ? cart.items.map(i => Number(i.actID)) : []).catch(e => message(e.message, true));
      $('cart-item-count').textContent = cart.items.length ? String(cart.items.length).padStart(2,'0') : '';
      $('cart-toolbar').hidden = !cart.items.length;
      if (!cart.items.length) {
        const empty = node('div', undefined, $('cart-items'), 'nv-cart-empty');
        node('span','＋',empty,'nv-cart-empty-mark');
        node('h2','下一場期待，還在等你',empty);
        node('p','購物車目前沒有活動，去找一場喜歡的演出吧。',empty);
        node('a','探索活動 →',empty,'nv-button').href='activities.html';
      }
      const groups = new Map();
      for (const item of cart.items) {
        const pub = Number(item.pubID || 0);
        if (!groups.has(pub)) {
          const section = node('section', undefined, $('cart-items'), 'nv-cart-group');
          const heading = node('header', undefined, section, 'nv-cart-group-head');
          const title = node('div',undefined,heading);
          node('span','活動主辦',title,'nv-cart-group-kicker');
          node('h2', item.publisher || '活動主辦', title);
          const groupItems = cart.items.filter(i => Number(i.pubID || 0) === pub),
            groupSelected = groupItems.filter(i => cartScope.includes(Number(i.actID)));
          const subtotal = groupSelected.reduce((sum, i) => sum + Number(i.price || 0) * i.quantity, 0),
            reduction = cartCoupon !== '0' && Number(cart.coupon.targetPubID) === pub ? Number(cart.discount) : 0;
          action('只選此廠商', heading, () => cartPage(cartCoupon, groupItems.map(i => Number(i.actID))), 'nv-cart-text-button');
          const groupRows=node('div',undefined,section,'nv-cart-group-rows');
          const footer=node('footer',undefined,section,'nv-cart-group-total');
          node('span','本組已選 ' + groupSelected.length + ' 個活動',footer);
          const amount=node('div',undefined,footer);
          if(reduction) node('small','已折抵 ' + money(reduction),amount);
          node('strong',money(subtotal-reduction),amount);
          groups.set(pub, groupRows);
        }
        const box = node('article', undefined, groups.get(pub), 'nv-cart-item');
        box.classList.toggle('is-unselected',!item.selected);
        const selection=node('div',undefined,box,'nv-cart-item-check');
        const choose = node('input', undefined, selection);
        choose.type = 'checkbox';
        choose.checked = !!item.selected;
        choose.dataset.cartAct = item.actID;
        choose.setAttribute('aria-label', '結帳 ' + (item.actName || '活動'));
        choose.onchange = () => {
          const ids = new Set(cartScope);
          if (choose.checked) ids.add(Number(item.actID));
          else ids.delete(Number(item.actID));
          cartPage(cartCoupon, [...ids]).catch(e => message(e.message, true));
        };
        const poster=node('a',undefined,box,'nv-cart-poster');
        poster.href='activity-detail.html?actID=' + item.actID;
        poster.setAttribute('aria-label','查看活動：'+(item.actName || '活動'));
        const artwork=image('ActivityImage?actID='+item.actID,item.actName || '活動海報',poster);
        artwork.onerror=()=>{artwork.hidden=true;node('span','NEW\nVIEW',poster);};
        const content=node('div',undefined,box,'nv-cart-item-content');
        node('span',item.seatMode?'指定座位':'非指定座位',content,'nv-cart-item-kind');
        node('a',item.actName || '活動資料無法使用',node('h3',undefined,content)).href=poster.href;
        const meta=[item.actDate && date(item.actDate),item.time && String(item.time).slice(0,5),item.venue].filter(Boolean);
        if(meta.length)node('p',meta.join(' · '),content,'nv-cart-item-meta');
        if (Number(item.ended)) node('p', '活動已結束，請移除或取消勾選。', content, 'nv-cart-item-warning');
        if (item.seatMode) {
          const seatline=node('div',undefined,content,'nv-cart-seat-tags');
          if(!item.seats.length)node('span','尚未選位',seatline,'nv-cart-item-warning');
          for(const seat of item.seats)node('span',seat,seatline,'nv-cart-seat-tag');
          const status=item.seatsValid ? '保留至 ' + new Date(item.holdExpiresAt).toLocaleTimeString('zh-TW') : item.seats.length?'座位保留已逾時，請重新選位':'請先選擇座位，再進行結帳';
          node('p',status,content,item.seatsValid?'nv-cart-seat-status':'nv-cart-item-warning');
        }
        const controls=node('div',undefined,content,'nv-cart-item-controls');
        const form = node('form', undefined, controls,'nv-cart-quantity'),
          inputId = 'cart-quantity-' + item.actID;
        const quantityLabel=node('label', '數量', form);quantityLabel.htmlFor = inputId;
        const input = node('input', undefined, form, 'form-control');
        Object.assign(input, { id: inputId, type: 'number', min: '1', max: '10', required: true, value: item.quantity, disabled: !!item.seatMode });
        const save = node('button', '更新數量', form, 'nv-cart-text-button');
        save.type = 'submit';
        if(item.seatMode){form.hidden=true;node('span',item.quantity+' 張票券',controls,'nv-cart-ticket-count');}
        form.onsubmit = e => {
          e.preventDefault();
          cartMutation({action: 'update',actID: item.actID,quantity: input.value}, cartScope, '購物車數量已更新。').catch(e => message(e.message, true));
        };
        if(item.seatMode)node('a','更換座位',controls,'nv-cart-text-link').href='seats.html?actID='+item.actID;
        const remove=action('移除', controls, () => cartMutation({action: 'remove',actID: item.actID}, cartScope.filter(id => id !== Number(item.actID)), '已移除活動。'), 'nv-cart-text-button nv-cart-remove');
        remove.setAttribute('aria-label','移除 '+(item.actName || '活動'));
        const price=node('div',undefined,box,'nv-cart-item-price');
        node('strong',money(Number(item.price)*item.quantity),price);
        node('small','每張 '+money(item.price),price);
      }
      $('cart-summary-selection').textContent='已選 '+cartScope.length+' 個活動 · '+cart.items.filter(i=>i.selected).reduce((n,i)=>n+Number(i.quantity),0)+' 張票券';
      $('cart-subtotal').textContent=money(cart.total);
      $('cart-reduction').textContent='− '+money(cart.discount);
      $('cart-problem').hidden=cart.valid;
      $('cart-problem').textContent=cart.valid?'':cart.problem;
      $('cart-discount').textContent = '已選 ' + cartScope.length + ' 個活動 · 原價 ' + money(cart.total) + ' · 優惠折抵 ' + money(cart.discount);
      $('cart-total').textContent = cart.valid ? money(cart.payable) : '—';
      $('checkout-submit').textContent = '確認結帳';
      $('checkout-submit').disabled = !cartScope.length || !cart.valid;
      $('confirm-simulation').checked = false;
      message(!cart.valid ? cart.problem : notice, !cart.valid || !!notice);
      const expiry = cart.items.filter(i => i.selected && i.seatMode && i.seatsValid).map(i => i.holdExpiresAt);
      $('cart-hold-note').hidden=!expiry.length;
      function holdNote(){
        const seconds=Math.max(0,Math.ceil((Math.min(...expiry)-Date.now())/1000));
        $('cart-hold-note').textContent='座位保留 '+Math.floor(seconds/60)+':'+String(seconds%60).padStart(2,'0')+' · 請於期限內完成結帳';
      }
      if(expiry.length)holdNote();
      if (expiry.length) cartTimer = setInterval(() => {
        holdNote();
        if (Date.now() >= Math.min(...expiry)) {
          clearInterval(cartTimer);
          $('checkout-submit').disabled = true;
          cartPage(cartCoupon,cartScope).catch(e=>message(e.message,true));
          message('勾選座位的保留時間已到，已釋出座位。請重新選位，或取消勾選該活動。', true);
        }
      }, 1000);
      $('checkout-form').onsubmit = async e => {
        e.preventDefault();
        if (cartBusy) return;
        cartBusy = true;
        const thaw = freezeCart();
        $('checkout-submit').textContent = '正在建立訂單…';
        try {
          const result = await api('LocalCheckout', {
            requestID: cart.requestID,
            actIDs: cart.actIDs,
            useDisID: cart.coupon.useDisID,
            confirmSimulation: String($('confirm-simulation').checked)
          });
          location.href = 'checkout_thankupage.html?requestID=' + encodeURIComponent(result.requestID);
        } catch(error){
          if(error.status===0||error.status>=500){
            try{await api('LocalCheckout?requestID='+encodeURIComponent(cart.requestID));location.href='checkout_thankupage.html?requestID='+encodeURIComponent(cart.requestID);return;}
            catch(lookup){if(lookup.status!==404){thaw();cartBusy=false;message('目前無法確認本次訂單結果，請先到「我的訂單」查詢，避免重複操作。',true);return;}}
          }
          thaw();cartBusy=false;await cartPage(cartCoupon,cartScope);message(error.message,true);
        }
      };
    } finally {
      unlock();
      cartBusy = false;
    }
  }
  async function checkoutReceipt() {
    const key = params.get('requestID');
    if (!key) {
      message('請先從購物車結帳。', true);
      return;
    }
    const receipt = await api('LocalCheckout?requestID=' + encodeURIComponent(key)),
      orders = await api('MemberOrders');
    const selected=orders.filter(o=>receipt.orderIDs.includes(o.orderID));
    if(selected.length!==receipt.orderIDs.length)throw new Error('部分訂單資料無法讀取，請到我的訂單確認。');
    const summary=$('receipt-summary');
    node('span','本次共 '+selected.length+' 筆訂單 · '+selected.reduce((n,o)=>n+Number(o.actQuantity||0),0)+' 張票券',summary);
    const total=node('div',undefined,summary);node('small','建立時訂單總額',total);node('strong',money(selected.reduce((n,o)=>n+Number(o.ordTotal)-Number(o.discount||0),0)),total);
    for (const order of selected) {
      const box=node('article',undefined,$('checkout-receipt'),'nv-receipt-order nv-form-card');
      const head=node('header',undefined,box,'nv-receipt-order-head');
      const title=node('div',undefined,head);node('small',order.publisher||'活動主辦',title);node('h2','訂單 #'+order.orderID,title);
      node('span',Number(order.ordType)===2?'已取消':Number(order.ordType)===1?'已使用':Number(order.usedCount)>0?'部分票券已使用':'已成立，待使用',head,'nv-member-status');
      orderContents(order,box);
      const totals=node('dl',undefined,box,'nv-member-order-amounts');
      for(const [label,value] of [['原價',money(order.ordTotal)],['優惠折抵',money(order.discount||0)],['訂單金額',money(Number(order.ordTotal)-Number(order.discount||0))]]){const part=node('div',undefined,totals);node('dt',label,part);node('dd',value,part);}
      const actions=node('div',undefined,box,'nv-receipt-order-actions');
      node('a','查看本筆訂單票券',actions,'nv-button').href='tickets.html?orderID='+order.orderID;
      node('a','查看訂單 #'+order.orderID+' 明細 →',actions).href='myOrder.html?orderID='+order.orderID;
    }
  }

  async function seatPage() {
    const id = params.get('actID');
    if (!id || !/^\d+$/.test(id)) {
      message('請從活動選擇座位。', true);
      return;
    }
    const activity = await api('ActivityDetail?actID=' + id);
    $('seat-activity-name').textContent = activity.actName;
    $('seat-back-link').href='activity-detail.html?actID='+id;
    $('seat-activity-meta').textContent = (date(activity.actDate) || '') + ' · ' + (activity.cityAddress || '') + ' · 每張 ' + money(activity.actPrice);
    let chosen = new Set(),
      initialized = false,
      holdDeadline = 0,
      inFlight = false;
    let seatSize=42, layoutCols=6;
    function zoom(){
      $('seat-grid').style.gridTemplateColumns='32px repeat('+layoutCols+', '+seatSize+'px)';
      $('seat-grid').style.minWidth=(38+layoutCols*(seatSize+6))+'px';
      $('seat-grid').style.setProperty('--seat-size',seatSize+'px');
      $('seat-zoom-label').textContent=Math.round(seatSize/42*100)+'%';
      $('seat-zoom-out').disabled=seatSize<=28;
      $('seat-zoom-in').disabled=seatSize>=56;
    }
    $('seat-zoom-out').onclick=()=>{seatSize=Math.max(28,seatSize-7);zoom();};
    $('seat-zoom-in').onclick=()=>{seatSize=Math.min(56,seatSize+7);zoom();};
    try {
      const draft = JSON.parse(sessionStorage.getItem('seat-draft-' + id) || 'null');
      if (draft && Date.now() - draft.at < 600000) chosen = new Set(draft.seats);
    } catch {}
    async function refresh() {
      const data = await api('Seats?actID=' + id);
      const scrollFrame=document.querySelector('.nv-seat-scroll');
      const scrollTop=scrollFrame.scrollTop,scrollLeft=scrollFrame.scrollLeft;
      const focusedSeat=$('seat-grid').contains(document.activeElement)?document.activeElement.dataset.seat:null;
      $('seat-grid').replaceChildren();
      const positions = data.seats.map(seat => {
        const match = /^([A-Z]+)([0-9]+)$/.exec(seat.seat);
        if (!match) return null;
        return {row: [...match[1]].reduce((n,c) => n * 26 + c.charCodeAt(0) - 64, 0), col: Number(match[2]), label: match[1]};
      });
      const regular = positions.length && positions.every(p => p && p.col > 0) && Math.min(...positions.map(p => p.row)) === 1;
      const cols = regular ? Math.max(...positions.map(p => p.col)) : 6;
      const rows = regular ? Math.max(...positions.map(p => p.row)) : Math.ceil(data.seats.length / cols);
      layoutCols=cols;zoom();
      $('seat-layout-info').textContent = (rows === 10 && cols === 10 ? '小型場地' : rows === 20 && cols === 20 ? '中型場地' : rows === 30 && cols === 30 ? '大型場地' : '座位配置') + ' · ' + rows + ' 排 × ' + cols + ' 欄 · 可售座位 ' + data.seats.length + ' 個';
      const labels = new Map();
      if (regular) for (const p of positions) labels.set(p.row,p.label);
      else for (let r=1;r<=rows;r++) labels.set(r,String(r));
      for (const [r,label] of labels) {
        const row = node('span',label + '排',$('seat-grid'),'nv-seat-row-label');
        row.style.gridRow = String(r);row.style.gridColumn = '1';
      }
      let remaining = [];
      let seatIndex = 0;
      for (const seat of data.seats) {
        if (!initialized && seat.state === 'mine') chosen.add(seat.seat);
        if (['held', 'booked'].includes(seat.state)) chosen.delete(seat.seat);
        if (seat.state === 'mine') remaining.push(seat.remainingMs);
        const symbol = {
          available: '○',
          mine: '●',
          held: '▧',
          booked: '×'
        } [seat.state];
        const button = node('button', symbol + ' ' + seat.seat, $('seat-grid'), 'nv-button nv-button-outline');
        button.type = 'button';
        const pos = regular ? positions[seatIndex] : {row:Math.floor(seatIndex / cols)+1,col:seatIndex % cols+1};
        button.style.gridRow = String(pos.row);button.style.gridColumn = String(pos.col+1);
        seatIndex++;
        button.dataset.seat = seat.seat;
        button.dataset.state=seat.state;
        button.disabled = ['held', 'booked'].includes(seat.state);
        button.setAttribute('aria-label', seat.seat + ' ' + ({
          available: '可選',
          mine: '你的暫鎖',
          held: '他人暫鎖',
          booked: '模擬已分配'
        })[seat.state]);
        button.setAttribute('aria-pressed', String(chosen.has(seat.seat)));
        if (chosen.has(seat.seat)) button.style.background = '#e8d5b9';
        button.onclick = () => {
          if (chosen.has(seat.seat)) chosen.delete(seat.seat);
          else if (chosen.size < 10) chosen.add(seat.seat);
          else {
            message('每次最多選 10 個座位。', true);
            return;
          }
          button.setAttribute('aria-pressed', String(chosen.has(seat.seat)));
          button.style.background = chosen.has(seat.seat) ? '#e8d5b9' : '';
          summary();
        };
      }
      scrollFrame.scrollTop=scrollTop;scrollFrame.scrollLeft=scrollLeft;
      if(focusedSeat){const button=[...$('seat-grid').querySelectorAll('button')].find(b=>b.dataset.seat===focusedSeat);if(button)button.focus({preventScroll:true});}
      initialized = true;
      holdDeadline = remaining.length ? Date.now() + Math.min(...remaining) : 0;
      countdown();
      summary();
    }

    function summary() {
      $('seat-selected').textContent = '已選 ' + chosen.size + ' 張票券';
      $('seat-selected-codes').replaceChildren();
      if(!chosen.size)node('p','點選座位圖，挑選你的觀賞位置。',$('seat-selected-codes'),'nv-muted');
      for(const code of [...chosen].sort())node('span',code,$('seat-selected-codes'),'nv-cart-seat-tag');
      $('seat-total').textContent=money(Number(activity.actPrice)*chosen.size);
      $('seat-hold').textContent=session.userID?'保留座位並加入購物車':'登入並保留座位';
      $('seat-hold').disabled = inFlight || !chosen.size || Number(activity.ended) === 1;
      $('seat-release').disabled = inFlight || !session.userID;
    }

    function countdown() {
      const seconds = Math.max(0, Math.ceil((holdDeadline - Date.now()) / 1000));
      $('seat-expiry').textContent = Number(activity.ended) === 1 ? '活動已結束，無法保留座位。' : seconds ? '保留倒數 ' + Math.floor(seconds / 60) + ':' + String(seconds % 60).padStart(2, '0') + '（重新整理不會延長）' : '尚未保留座位；按下保留按鈕才會開始計時。';
    }
    const clock = setInterval(countdown, 1000);
    window.addEventListener('pagehide', () => clearInterval(clock), {
      once: true
    });
    $('seat-refresh').onclick = () => refresh().catch(e => message(e.message, true));
    $('seat-hold').onclick = async () => {
      if (inFlight) return;
      if (!session.userID) {
        sessionStorage.setItem('seat-draft-' + id, JSON.stringify({
          at: Date.now(),
          seats: [...chosen]
        }));
        location.href = loginUrl();
        return;
      }
      inFlight = true;
      summary();
      try {
        await api('Seats', {
          action: 'hold',
          actID: id,
          seats: [...chosen].sort().join(',')
        });
        sessionStorage.removeItem('seat-draft-' + id);
        location.href = 'checkout_cart.html';
      } catch (error) {
        inFlight=false;try{await refresh();}catch{}summary();message(error.message,true);
      }
    };
    $('seat-release').onclick = async () => {
      if (inFlight) return;
      inFlight = true;
      summary();
      try {
        await api('Seats', {
          action: 'release',
          actID: id
        });
        chosen.clear();
        initialized = false;
        inFlight = false;
        sessionStorage.removeItem('seat-draft-' + id);
        await refresh();
        message('已釋放座位並移除該活動購物車項目。');
      } catch (e) {
        inFlight = false;
        summary();
        message(e.message, true);
      }
    };
    await refresh();
    const timer = setInterval(() => {
      if (!document.hidden && !inFlight) refresh().catch(e => message(e.message, true));
    }, 10000);
    window.addEventListener('pagehide', () => clearInterval(timer), {
      once: true
    });
  }

  function ticketState(t) {
    return Number(t.orderState) === 2 ? '已取消' : Number(t.usedType) === 1 ? '已使用' : t.canRedeem ? '待使用' : '目前不可使用';
  }
  async function ticketList() {
    let tickets = await api('Tickets');
    const requested=params.get('orderID');
    if(requested){if(!/^[1-9][0-9]*$/.test(requested))throw new Error('訂單編號不正確。');await api('MemberOrders?orderID='+requested);tickets=tickets.filter(t=>String(t.orderID)===requested);const context=$('ticket-order-context');context.hidden=false;node('strong','訂單 #'+requested+' · 共 '+tickets.length+' 張票券',context);node('a','查看本筆訂單 →',context).href='myOrder.html?orderID='+requested;node('a','查看全部票券',context).href='tickets.html';}
    const render = () => {
      $('ticket-list').replaceChildren();
      const filter = $('ticket-filter').value;
      const visible = tickets.filter(t => filter === 'all' || filter === 'ready' && t.canRedeem || filter === 'used' && Number(t.usedType) === 1 || filter === 'cancelled' && Number(t.orderState) === 2);
      if (!visible.length) node('p', '沒有符合條件的票券。', $('ticket-list'), 'nv-empty');
      for (const t of visible) {
        const box = node('article', undefined, $('ticket-list'), 'nv-form-card nv-member-ticket');
        node('span',ticketState(t),box,'nv-member-status'+(!t.canRedeem?' is-muted':''));
        node('a','所屬訂單 #'+t.orderID,box,'nv-ticket-order-number').href='myOrder.html?orderID='+t.orderID;
        node('small','票券 #'+t.ticketID,box,'nv-ticket-serial');
        node('h2', t.actName || '未提供活動', box);
        node('p',[date(t.actDate),String(t.time||'').slice(0,5),t.venue].filter(Boolean).join(' · '),box,'nv-muted');
        node('p',(t.publisher||'活動主辦')+' · '+money(t.amount),box,'nv-muted');
        node('p',t.seat?'座位 '+t.seat:'未指定座位',box,'nv-ticket-seat');
        node('a', '查看票券 #' + t.ticketID, box, 'nv-button nv-button-outline').href = 'ticket.html?ticketID=' + t.ticketID;
      }
    };
    $('ticket-filter').onchange = render;
    render();
  }
  async function ticketDetail() {
    const id = params.get('ticketID');
    if (!id || !/^\d+$/.test(id)) {
      message('請從我的票券選擇票券。', true);
      return;
    }
    const t = await api('Tickets?ticketID=' + id);
    $('local-ticket').hidden = false;
    $('ticket-name').textContent = t.actName || '本機票券';
    $('ticket-info').textContent='所屬訂單 #'+t.orderID+' ／ 票券 #'+t.ticketID;
    $('ticket-order-link').href='myOrder.html?orderID='+t.orderID;
    $('ticket-sibling-link').href='tickets.html?orderID='+t.orderID;
    for(const [label,value] of [['活動時間',(date(t.actDate)||'日期未提供')+' '+String(t.time||'').slice(0,5)],['場地',t.venue||'未提供'],['主辦單位',t.publisher||'未提供'],['座位',t.seat||'未指定'],['票券單價',money(t.amount)]]){const part=node('div',undefined,$('ticket-facts'));node('dt',label,part);node('dd',value,part);}
    $('ticket-status').textContent = ticketState(t) + (t.redeemedAt ? ' · 核銷時間 ' + date(t.redeemedAt) : '');
    await ticketReview(t.ticketID);
    if (t.canRedeem) {
      $('ticket-qr-area').hidden = false;
      $('ticket-qr').src = 'TicketQR?ticketID=' + t.ticketID;
      $('ticket-qr').onerror = () => {
        $('ticket-qr').hidden = true;
        message('QR 暫時無法顯示，請重新確認票券狀態。', true);
      };
      $('ticket-payload').value = t.payload;
      $('ticket-download').href = 'TicketQR?ticketID=' + t.ticketID;
      $('ticket-redeem-link').href = 'redeem-ticket.html?ticketID=' + t.ticketID;
    }
  }
  async function redeemTicket() {
    const tickets = await api('Tickets'),
      select = $('redeem-ticket-select');
    node('option', '請選擇票券，或貼上票券碼', select).value = '';
    for (const t of tickets) {
      const option = node('option', (t.actName || '活動') + ' · #' + t.ticketID + ' · ' + (t.seat || '自由入座') + ' · ' + ticketState(t), select);
      option.value = t.ticketID;
    }
    let preview, previewPayload = '',
      busy = false;

    function clearPreview() {
      preview = undefined;
      previewPayload = '';
      $('redeem-confirm-form').hidden = true;
      $('redeem-preview').replaceChildren();
      $('redeem-confirm').checked = false;
    }

    function show(t, payload = '') {
      clearPreview();
      preview = t;
      previewPayload = payload;
      const box = $('redeem-preview');
      node('h2', t.actName || '活動票券', box);
      node('p', '票券 #' + t.ticketID + ' · 訂單 #' + t.orderID, box);
      node('p', (date(t.actDate) || '日期未提供') + ' · ' + (t.seat ? '座位 ' + t.seat : '自由入座') + ' · ' + ticketState(t), box);
      if (t.redeemedAt) node('p', '核銷時間 ' + date(t.redeemedAt), box);
      $('redeem-confirm-form').hidden = !t.canRedeem;
      if (!t.canRedeem) node('p', '此票券目前無法核銷。', box, 'nv-muted');
    }
    select.onchange = async () => {
      clearPreview();
      if (!select.value) return;
      try {
        const t = await api('Tickets?ticketID=' + encodeURIComponent(select.value));
        $('redeem-payload').value = t.payload || '';
        if (t.canRedeem) {
          const p = await api('PreviewTicket', {
            payload: t.payload
          });
          show(p, t.payload);
        } else show(t);
      } catch (error) {
        message(error.message, true);
      }
    };
    $('redeem-payload').oninput = () => {
      select.value = '';
      clearPreview();
    };
    $('redeem-form').onsubmit = async e => {
      e.preventDefault();
      if (busy) return;
      busy = true;
      $('redeem-lookup').disabled = true;
      clearPreview();
      try {
        const payload = $('redeem-payload').value.trim(),
          t = await api('PreviewTicket', {
            payload
          });
        show(t, payload);
        message('請核對活動、座位與票券狀態，再確認核銷。');
      } catch (error) {
        message(error.message, true);
      } finally {
        busy = false;
        $('redeem-lookup').disabled = false;
      }
    };
    $('redeem-qr-file').onchange = async e => {
      clearPreview();
      const file = e.target.files[0];
      if (!file) return;
      try {
        if (file.size > 2 * 1024 * 1024 || !['image/png', 'image/jpeg'].includes(file.type)) throw new Error('請選擇 2 MB 以內的 PNG 或 JPEG QR 圖片。');
        const form = new FormData();
        form.append('image', file);
        const t = await api('DecodeTicketQR', form);
        $('redeem-payload').value = t.payload || '';
        select.value = String(t.ticketID);
        show(t, t.payload || '');
        message('已辨識 QR，請核對票券後確認核銷。');
      } catch (error) {
        message(error.message, true);
      } finally {
        e.target.value = '';
      }
    };
    $('redeem-confirm-form').onsubmit = async e => {
      e.preventDefault();
      if (busy || !preview?.canRedeem || !previewPayload) return;
      busy = true;
      $('redeem-submit').disabled = true;
      const payload = previewPayload;
      try {
        const result = await api('RedeemLocalTicket', {
          payload,
          confirmSimulation: String($('redeem-confirm').checked)
        });
        clearPreview();
        $('redeem-form').hidden = true;
        $('redeem-select-area').hidden = true;
        $('redeem-result').replaceChildren();
        node('h2', '核銷完成', $('redeem-result'));
        node('p', '票券 #' + result.ticketID + ' · ' + date(result.redeemedAt), $('redeem-result'));
        node('a', '查看此票券與活動評價', $('redeem-result'), 'nv-button').href = 'ticket.html?ticketID=' + result.ticketID;
        node('a', '繼續核銷其他票券', $('redeem-result'), 'nv-button nv-button-outline').href = 'redeem-ticket.html';
        message('票券已記錄為使用；展示流程不代表真實入場。');
      } catch (error) {
        clearPreview();
        message(error.message + ' 請重新查詢票券狀態。', true);
      } finally {
        busy = false;
        $('redeem-submit').disabled = false;
      }
    };
    if (params.get('ticketID')) {
      select.value = params.get('ticketID');
      await select.onchange();
    }
  }
  async function activityReviews(actID) {
    const data = await api('ActivityReviews?actID=' + actID);
    $('activity-review-section').hidden = false;
    $('activity-rating-summary').textContent = data.count ? '平均 ' + data.average + ' / 5，共 ' + data.count + ' 則評價' : '目前尚無評分。';
    $('activity-reviews').replaceChildren();
    for (const review of data.reviews) {
      const box = node('article', undefined, $('activity-reviews'), 'nv-form-card');
      node('p', review.author + ' · ' + review.rating + ' / 5 · ' + date(review.reviewedAt), box);
      node('p', review.content, box).style.whiteSpace = 'pre-wrap';
    }
  }
  async function ticketReview(ticketID) {
    const review = await api('TicketReview?ticketID=' + ticketID);
    $('ticket-review-display').replaceChildren();
    $('ticket-review-form').hidden = !review.canReview;
    if (review.reviewed) {
      node('p', '已評價：' + (review.rating || '未提供') + ' / 5' + (review.reviewedAt ? ' · ' + date(review.reviewedAt) : ''), $('ticket-review-display'));
      node('p', review.content || '', $('ticket-review-display')).style.whiteSpace = 'pre-wrap';
    } else if (!review.canReview) node('p', '完成模擬核銷且未取消的票券才可評價。', $('ticket-review-display'));
    $('ticket-review-form').onsubmit = async e => {
      e.preventDefault();
      $('review-submit').disabled = true;
      try {
        await api('TicketReview', {
          ticketID,
          rating: $('review-rating').value,
          content: $('review-content').value
        });
        await ticketReview(ticketID);
        message('活動評價已送出。');
      } catch (error) {
        message(error.message, true);
      } finally {
        $('review-submit').disabled = false;
      }
    };
  }
  async function boot() {
    setupHero();
    document.querySelectorAll(".nv-links a").forEach(link => {
      if (link.getAttribute("href") === path) link.setAttribute("aria-current", "page");
    });
    if ($('nav-toggle')) $('nav-toggle').onclick = () => {
      const open = $('nav-toggle').getAttribute('aria-expanded') !== 'true';
      $('nav-toggle').setAttribute('aria-expanded', String(open));
      $('site-links').classList.toggle('is-open', open);
    };
    try {
      session = await api('RecoverySession');
      categories = await api('GetPostCategoryServlet?action=list');
      if ($('member-link')) $('member-link').hidden = !session.userID;
      $('session-name').textContent = '';
      $('login-link').hidden = !!session.userID;
      $('login-link').href = path === 'sign-in.html' ? 'sign-in.html' : loginUrl();
      $('logout-button').hidden = !session.userID;
      $('logout-button').onclick = async () => {
        try {
          await api('RecoverySession', {});
          location.href = 'forum_home.html';
        } catch (e) {
          message(e.message, true);
        }
      };
      if (path === 'sign-in.html') {
        if (params.get('passwordUpdated')) message('密碼已更新，請重新登入。');
        if (params.get('registered')) message('註冊完成，請使用新帳號登入。');
        $('login-form').onsubmit = async e => {
          e.preventDefault();
          $('commit').disabled = true;
          try {
            await api('SignIn', {
              account: $('account').value.trim(),
              password: $('password').value
            });
            $('password').value = '';
            location.href = safeNext();
          } catch (error) {
            message(error.status===401?'帳號或密碼不正確，請確認後再試。':error.message, true);
            $('password').value = '';
            $('commit').disabled = false;
          }
        };
      } else if ($('change-password-form')) {
        if (!session.userID) {
          location.href = 'sign-in.html?next=change-password.html';
          return;
        }
        $('change-password-form').onsubmit = async e => {
          e.preventDefault();
          e.submitter.disabled = true;
          try {
            await api('UpdateUserPassword', Object.fromEntries(new FormData(e.target)));
            e.target.reset();
            location.href = 'sign-in.html?passwordUpdated=1';
          } catch (error) {
            message(error.message, true);
            $('oldPassword').value = '';
            e.submitter.disabled = false;
          }
        };
      } else if ($('request-reset-form')) {
        $('request-reset-form').onsubmit = async e => {
          e.preventDefault();
          e.submitter.disabled = true;
          try {
            const data = await api('RetrievePassword', Object.fromEntries(new FormData(e.target)));
            message('');
            $('reset-code').textContent = data.message + (data.localCode ? ' 展示驗證碼：' + data.localCode : '');
            $('request-reset-form').hidden = true;
            $('reset-password-form').hidden = false;
          } catch (error) {
            message(error.message, true);
          } finally {
            e.submitter.disabled = false;
          }
        };
        $('reset-restart').onclick = () => {
          $('reset-password-form').reset();
          $('reset-password-form').hidden = true;
          $('request-reset-form').hidden = false;
          message('每次取得驗證碼須間隔 30 秒。');
        };
        $('reset-password-form').onsubmit = async e => {
          e.preventDefault();
          e.submitter.disabled = true;
          try {
            await api('VerifyPassword', Object.fromEntries(new FormData(e.target)));
            e.target.reset();
            location.href = 'sign-in.html?passwordUpdated=1';
          } catch (error) {
            message(error.message, true);
            e.submitter.disabled = false;
          }
        };
      } else if ($('register-form')) {
        $('register-form').onsubmit = async e => {
          e.preventDefault();
          const button = e.submitter;
          button.disabled = true;
          try {
            const data = await api('SignUp', Object.fromEntries(new FormData(e.target)));
            $('password').value = '';
            $('confirmPassword').value = '';
            $('local-code').textContent = `${data.message}。展示驗證碼：${data.localCode}`;
            $('register-form').hidden = true;
            $('verify-form').hidden = false;
            message('');
          } catch (error) {
            message(error.message, true);
          } finally {
            button.disabled = false;
          }
        };
        $('verify-form').onsubmit = async e => {
          e.preventDefault();
          e.submitter.disabled = true;
          try {
            await api('SignUp', {
              action: 'verify',
              code: $('code').value
            });
            location.href = 'sign-in.html?registered=1';
          } catch (error) {
            message(error.message, true);
            e.submitter.disabled = false;
          }
        };
        $('restart-register').onclick = () => {
          $('register-form').hidden = false;
          $('verify-form').hidden = true;
          $('code').value = '';
          message('');
        };
      } else if ($('ticket-list') || $('local-ticket') || $('redeem-form')) {
        if (!session.userID) {
          location.href = loginUrl();
          return;
        }
        if ($('ticket-list')) await ticketList();
        else if ($('local-ticket')) await ticketDetail();
        else await redeemTicket();
      } else if ($('member-coupons')) {
        if (!session.userID) {
          location.href = loginUrl();
          return;
        }
        await memberCoupons();
      } else if ($('seat-grid')) {
        await seatPage();
      } else if ($('cart-items') || $('checkout-receipt')) {
        if (!session.userID) {
          location.href = loginUrl();
          return;
        }
        if ($('cart-items')) await cartPage();
        else await checkoutReceipt();
      } else if ($('activity-catalog')) {
        await activityCatalog();
      } else if ($('activity-detail')) {
        await activityDetail();
      } else if ($('member-orders') || $('member-favorites')) {
        if (!session.userID) {
          location.href = loginUrl();
          return;
        }
        if ($('member-orders')) await memberOrders();
        else await memberFavorites();
      } else if ($('profile-form')) {
        if (!session.userID) {
          location.href = 'sign-in.html?next=member.html';
          return;
        }
        const profile = await api('GetUserInfo');
        $('member-account').textContent = profile.account;
        for (const key of ['name', 'nickname', 'birthdate', 'cellphone', 'email']) $(key).value = profile[key] || '';
        $('profile-form').onsubmit = async e => {
          e.preventDefault();
          e.submitter.disabled = true;
          try {
            await api('UpdateUserInfo', Object.fromEntries(new FormData(e.target)));
            $('profile-result').textContent = '會員資料已儲存。';
            message('');
          } catch (error) {
            message(error.message, true);
          } finally {
            e.submitter.disabled = false;
          }
        };
      } else if ($('latest-posts')) await home();
      else if ($('postform')) await editor();
      else if ($('postHeader')) await detail();
    } catch (error) {
      message(error.message, true);
    }
  }
  boot();
})();
