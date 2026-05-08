import React from 'react';
import ReactDOM from 'react-dom/client';
import {
  Camera,
  Heart,
  Lock,
  LogIn,
  MapPin,
  MessageCircle,
  PawPrint,
  Pencil,
  Plus,
  Search,
  ShieldCheck,
  Store,
  Tags,
  Trash2,
  User,
  X
} from 'lucide-react';
import './styles.css';

const API_BASE = '/api';
const CONTACT_VALUE = '站内私信';
const phonePattern = /(?:\+?86[-\s]?)?1[3-9]\d{9}/;
const MAX_IMAGE_SIZE = 5 * 1024 * 1024;
const allowedImageTypes = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];

type AppUser = { id: number; nickname: string };
type Category = { id: number; name: string; description: string; tags: string };
type Pet = {
  id: number;
  name: string;
  category: string;
  breed: string;
  age: string;
  city: string;
  status: string;
  price: number;
  imageUrl: string;
  healthInfo: string;
  personality: string;
  createdAt?: string;
};
type MarketPost = {
  id: number;
  title: string;
  type: string;
  category: string;
  city: string;
  description: string;
  author: string;
  contact: string;
  imageUrl: string;
  createdAt?: string;
};
type Moment = {
  id: number;
  author: string;
  petName: string;
  category?: string;
  city?: string;
  content: string;
  likes: number;
  imageUrl: string;
  createdAt?: string;
};
type Region = { name: string; cities: Array<{ name: string; districts: string[] }> };
type ReferenceData = {
  regions: Region[];
  postTypes: string[];
  petStatuses: string[];
  petGenders: string[];
  ageRanges: string[];
  healthRecords: string[];
  personalityTags: string[];
  serviceTags: string[];
};

const fallbackRegions: Region[] = [
  { name: '上海市', cities: [{ name: '上海市', districts: ['浦东新区', '徐汇区', '静安区', '闵行区'] }] },
  { name: '浙江省', cities: [{ name: '杭州市', districts: ['西湖区', '拱墅区', '滨江区', '余杭区'] }] },
  { name: '江苏省', cities: [{ name: '南京市', districts: ['玄武区', '秦淮区', '建邺区'] }] }
];

const fallbackReferenceData: ReferenceData = {
  regions: fallbackRegions,
  postTypes: ['互换', '售卖', '领养', '闲置', '求助', '寄养', '寻宠'],
  petStatuses: ['在售', '可领养', '可互换'],
  petGenders: ['公', '母', '未知'],
  ageRanges: ['幼年', '青年', '成年', '老年'],
  healthRecords: ['疫苗齐全', '已驱虫', '已绝育', '体检正常'],
  personalityTags: ['亲人', '安静', '活泼', '胆小', '独立'],
  serviceTags: ['站内私信', '同城自提', '线下看宠']
};

const demoCategories: Category[] = [
  { id: 1, name: '猫咪', description: '温顺亲人，适合公寓和家庭陪伴。', tags: '新手友好,安静,陪伴型' },
  { id: 2, name: '狗狗', description: '活泼忠诚，需要规律运动和训练。', tags: '互动强,需要遛弯,家庭型' },
  { id: 3, name: '小宠', description: '仓鼠、兔子、龙猫等，占地小但需要细心照顾。', tags: '空间小,易观察,轻陪伴' },
  { id: 4, name: '用品', description: '食品、玩具、猫爬架、牵引绳等宠物用品。', tags: '闲置交易,日常消耗,养宠装备' }
];

const demoPets: Pet[] = [
  { id: 1, name: '团子', category: '猫咪', breed: '英短银渐层', age: '8个月', city: '上海市 上海市 浦东新区', status: '在售', price: 1800, imageUrl: '', healthInfo: '疫苗齐全，已驱虫', personality: '安静亲人，喜欢陪睡' },
  { id: 2, name: '可乐', category: '狗狗', breed: '柯基', age: '1岁', city: '浙江省 杭州市 西湖区', status: '可互换', price: 0, imageUrl: '', healthInfo: '体检正常，精力充沛', personality: '活泼黏人，会坐下握手' }
];

const demoPosts: MarketPost[] = [
  { id: 1, title: '想给柯基找同城互换寄养伙伴', type: '互换', category: '狗狗', city: '浙江省 杭州市 西湖区', description: '工作日偶尔出差，希望找同城稳定互助家庭。', author: '林小满', contact: CONTACT_VALUE, imageUrl: '' }
];

const demoMoments: Moment[] = [
  { id: 1, author: '林小满', petName: '团子', category: '猫咪', content: '今天第一次学会自己开零食罐。', likes: 28, imageUrl: '' }
];

function useApi<T>(path: string, fallback: T) {
  const [data, setData] = React.useState<T>(fallback);
  const [loading, setLoading] = React.useState(true);
  const load = React.useCallback(() => {
    setLoading(true);
    fetch(`${API_BASE}${path}`)
      .then((res) => res.json())
      .then(setData)
      .catch(() => setData(fallback))
      .finally(() => setLoading(false));
  }, [path, fallback]);
  React.useEffect(load, [load]);
  return { data, loading, reload: load };
}

function App() {
  const categories = useApi<Category[]>('/categories', demoCategories);
  const pets = useApi<Pet[]>('/pets', demoPets);
  const posts = useApi<MarketPost[]>('/posts', demoPosts);
  const moments = useApi<Moment[]>('/moments', demoMoments);
  const referenceData = useApi<ReferenceData>('/reference-data', fallbackReferenceData);
  const [searchQuery, setSearchQuery] = React.useState('');
  const [categoryFilter, setCategoryFilter] = React.useState('全部');
  const [cityFilter, setCityFilter] = React.useState('全部');
  const [typeFilter, setTypeFilter] = React.useState('全部');
  const [sortMode, setSortMode] = React.useState<'latest' | 'oldest'>('latest');
  const [currentUser, setCurrentUser] = React.useState<AppUser | null>(() => {
    const raw = localStorage.getItem('petshop_user');
    return raw ? JSON.parse(raw) : null;
  });
  const [detail, setDetail] = React.useState<{ type: 'post' | 'moment' | 'pet'; item: MarketPost | Moment | Pet } | null>(null);
  const [editing, setEditing] = React.useState<{ type: 'post' | 'moment'; item: MarketPost | Moment } | null>(null);
  const availableCategories = categories.data.map((category) => category.name);
  const availableCities = cityOptions(referenceData.data.regions.length ? referenceData.data.regions : fallbackRegions);
  const filteredCategories = categories.data.filter((category) => matchesText(searchQuery, [category.name, category.description, category.tags]));
  const filteredPets = sortByTime(pets.data
    .filter((pet) => matchesCategory(categoryFilter, pet.category))
    .filter((pet) => matchesCity(cityFilter, pet.city))
    .filter((pet) => matchesText(searchQuery, [pet.name, pet.category, pet.breed, pet.city, pet.status, pet.healthInfo, pet.personality])), sortMode);
  const filteredPosts = sortByTime(posts.data
    .filter((post) => matchesCategory(categoryFilter, post.category))
    .filter((post) => matchesCity(cityFilter, post.city))
    .filter((post) => matchesType(typeFilter, post.type))
    .filter((post) => matchesText(searchQuery, [post.title, post.type, post.category, post.city, post.description, post.author])), sortMode);
  const filteredMoments = sortByTime(moments.data
    .filter((moment) => matchesCategory(categoryFilter, moment.category))
    .filter((moment) => matchesCity(cityFilter, moment.city))
    .filter((moment) => matchesText(searchQuery, [moment.author, moment.petName, moment.category, moment.city, moment.content])), sortMode);

  function handleLogin(user: AppUser) {
    localStorage.setItem('petshop_user', JSON.stringify(user));
    setCurrentUser(user);
  }

  function logout() {
    localStorage.removeItem('petshop_user');
    setCurrentUser(null);
  }

  const reloadFeeds = () => {
    posts.reload();
    moments.reload();
  };

  return (
    <main>
      <header className="topbar">
        <div className="brand"><PawPrint /><span>萌宠集市</span></div>
        <nav>
          <a href="#categories">分类</a>
          <a href="#pets">展示</a>
          <a href="#market">售卖互换</a>
          <a href="#moments">日常</a>
          <a href="#mine">我的</a>
        </nav>
        <LoginBox currentUser={currentUser} onLogin={handleLogin} onLogout={logout} />
      </header>

      <section className="hero">
        <div className="heroCopy">
          <p className="eyebrow">宠物百科 · 站内沟通 · 同城社区</p>
          <h1>把宠物展示、交易互换和日常分享放在一个清爽空间里</h1>
          <p className="lead">登录后可发布和管理自己的内容。交易沟通固定使用站内私信，前后端都会拦截手机号。</p>
          <div className="search"><Search size={20} /><input value={searchQuery} onChange={(event) => setSearchQuery(event.target.value)} placeholder="搜索猫咪、柯基、互换、领养、闲置用品" /></div>
          <div className="quickFilters">
            <select value={categoryFilter} onChange={(event) => setCategoryFilter(event.target.value)}>
              <option value="全部">全部分类</option>
              {availableCategories.map((category) => <option key={category}>{category}</option>)}
            </select>
            <select value={cityFilter} onChange={(event) => setCityFilter(event.target.value)}>
              <option value="全部">全部城市</option>
              {availableCities.map((city) => <option key={city}>{city}</option>)}
            </select>
            <select value={typeFilter} onChange={(event) => setTypeFilter(event.target.value)}>
              <option value="全部">全部类型</option>
              {referenceData.data.postTypes.map((type) => <option key={type}>{type}</option>)}
            </select>
            <select value={sortMode} onChange={(event) => setSortMode(event.target.value as 'latest' | 'oldest')}>
              <option value="latest">最新发布</option>
              <option value="oldest">最早发布</option>
            </select>
          </div>
        </div>
        <div className="heroPanel">
          <Metric value={categories.data.length} label="分类库" />
          <Metric value={pets.data.length} label="展示宠物" />
          <Metric value={posts.data.length} label="交易帖子" />
        </div>
      </section>

      <section id="categories" className="section">
        <SectionTitle icon={<Tags />} title="宠物分类库" helper="发布内容时必须从平台分类库中选择" />
        <div className="categoryGrid">
          {categories.loading && <p>正在加载分类...</p>}
          {filteredCategories.map((category) => (
            <article className="category" key={category.id}>
              <h3>{category.name}</h3>
              <p>{category.description}</p>
              <div className="chips">{category.tags.split(',').map((tag) => <span key={tag}>{tag}</span>)}</div>
            </article>
          ))}
          {!categories.loading && filteredCategories.length === 0 && <EmptyState title="没有匹配的分类" helper="换个关键词或清空筛选条件再试。" />}
        </div>
      </section>

      <section id="pets" className="section">
        <SectionTitle icon={<Store />} title="宠物展示与售卖" helper="查看宠物基础信息，后续可扩展订单与审核" />
        <div className="petGrid">
          {filteredPets.map((pet) => (
            <article className="petCard" key={pet.id} onClick={() => setDetail({ type: 'pet', item: pet })}>
              {imageBox(pet.imageUrl, pet.name)}
              <div className="petInfo">
                <div className="between"><h3>{pet.name}</h3><span className="status">{pet.status}</span></div>
                <p className="petBreed">{pet.breed} · {pet.age}</p>
                <p className="sub"><MapPin size={15} />{pet.city}</p>
                <p>{pet.personality}</p>
                <div className="between cardFooter">
                  <span className="price">{pet.price > 0 ? `￥${pet.price}` : '面议'}</span>
                  <span className="health"><ShieldCheck size={15} />{pet.healthInfo}</span>
                </div>
              </div>
            </article>
          ))}
          {filteredPets.length === 0 && <EmptyState title="没有匹配的宠物" helper="可以调整分类、城市或关键词筛选。" />}
        </div>
      </section>

      <section id="market" className="section split">
        <div>
          <SectionTitle icon={<Plus />} title="售卖 / 互换 / 领养帖子" helper="点击帖子可查看详情，联系入口统一为站内私信" />
          <PostList posts={filteredPosts} onOpen={(post) => setDetail({ type: 'post', item: post })} />
        </div>
        <Composer categories={categories.data} referenceData={referenceData.data} currentUser={currentUser} onSuccess={reloadFeeds} />
      </section>

      <section id="moments" className="section">
        <SectionTitle icon={<Camera />} title="用户日常分享" helper="记录宠物近况、养护经验和可爱的日常瞬间" />
        <MomentList moments={filteredMoments} onOpen={(moment) => setDetail({ type: 'moment', item: moment })} />
      </section>

      <section id="mine" className="section">
        <SectionTitle icon={<User />} title="我的发布" helper="登录后可集中查看并删除自己的交易帖和日常" />
        <MyPanel currentUser={currentUser} posts={posts.data} moments={moments.data} onOpen={setDetail} onEdit={setEditing} onChanged={reloadFeeds} />
      </section>

      {detail && <DetailModal detail={detail} onClose={() => setDetail(null)} />}
      {editing && <EditModal detail={editing} categories={categories.data} referenceData={referenceData.data} currentUser={currentUser} onClose={() => setEditing(null)} onSaved={() => { setEditing(null); reloadFeeds(); }} />}
    </main>
  );
}

function LoginBox({ currentUser, onLogin, onLogout }: { currentUser: AppUser | null; onLogin: (user: AppUser) => void; onLogout: () => void }) {
  const [name, setName] = React.useState('');
  const [error, setError] = React.useState('');
  const [busy, setBusy] = React.useState(false);

  if (currentUser) {
    return <div className="userBadge"><User size={16} /><span>{currentUser.nickname}</span><button type="button" onClick={onLogout}>退出</button></div>;
  }

  async function submit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const nickname = name.trim();
    setError('');
    if (!nickname) return setError('请输入昵称');
    if (phonePattern.test(nickname)) return setError('昵称不能使用手机号');
    setBusy(true);
    try {
      const res = await fetch(`${API_BASE}/users/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ nickname })
      });
      if (!res.ok) throw new Error(await readError(res));
      onLogin(await res.json());
      setName('');
    } catch (err) {
      setError(err instanceof Error ? err.message : '登录失败，请检查后端服务。');
    } finally {
      setBusy(false);
    }
  }

  return (
    <form className="loginBox" onSubmit={submit} title={error || '登录后可发布'}>
      <LogIn size={16} />
      <input value={name} onChange={(event) => setName(event.target.value)} placeholder={error || '昵称登录'} />
      <button type="submit" disabled={busy}>{busy ? '...' : '登录'}</button>
    </form>
  );
}

function PostList({ posts, onOpen }: { posts: MarketPost[]; onOpen: (post: MarketPost) => void }) {
  if (posts.length === 0) {
    return <EmptyState title="没有匹配的交易帖" helper="可以调整交易类型、城市、分类或关键词。" />;
  }

  return (
    <div className="postList">
      {posts.map((post) => (
        <article className="post clickable" key={post.id} onClick={() => onOpen(post)}>
          <div className="between"><span className="type">{post.type}</span><span className="postContact"><MessageCircle size={15} />{post.contact || CONTACT_VALUE}</span></div>
          <h3>{post.title}</h3>
          <p>{post.description}</p>
          <div className="postMeta"><span>{post.category}</span><span>{post.city}</span><span>{post.author}</span></div>
        </article>
      ))}
    </div>
  );
}

function MomentList({ moments, onOpen }: { moments: Moment[]; onOpen: (moment: Moment) => void }) {
  if (moments.length === 0) {
    return <EmptyState title="没有匹配的日常" helper="可以调整城市、分类或关键词筛选。" />;
  }

  return (
    <div className="momentGrid">
      {moments.map((moment) => (
        <article className="moment clickable" key={moment.id} onClick={() => onOpen(moment)}>
          {imageBox(moment.imageUrl, moment.petName || moment.author)}
          <div><span className="type">{moment.category || '日常'}</span><h3>{moment.author} 和 {moment.petName}</h3><p>{moment.content}</p><div className="momentMeta"><span>{moment.city || '未选择地区'}</span><span className="likes"><Heart size={15} />{moment.likes}</span></div></div>
        </article>
      ))}
    </div>
  );
}

function MyPanel(props: {
  currentUser: AppUser | null;
  posts: MarketPost[];
  moments: Moment[];
  onOpen: (detail: { type: 'post' | 'moment' | 'pet'; item: MarketPost | Moment | Pet }) => void;
  onEdit: (detail: { type: 'post' | 'moment'; item: MarketPost | Moment }) => void;
  onChanged: () => void;
}) {
  if (!props.currentUser) return <div className="myPanel emptyState">登录后，这里会显示你发布的交易帖和日常。</div>;
  const myPosts = props.posts.filter((post) => post.author === props.currentUser!.nickname);
  const myMoments = props.moments.filter((moment) => moment.author === props.currentUser!.nickname);

  async function remove(kind: 'posts' | 'moments', id: number) {
    const author = encodeURIComponent(props.currentUser!.nickname);
    const res = await fetch(`${API_BASE}/${kind}/${id}?author=${author}`, { method: 'DELETE' });
    if (!res.ok) alert(await readError(res));
    props.onChanged();
  }

  return (
    <div className="myPanel">
      <div className="mySummary">
        <div><strong>{myPosts.length}</strong><span>我的帖子</span></div>
        <div><strong>{myMoments.length}</strong><span>我的日常</span></div>
      </div>
      <div className="myColumns">
        <div>
          <h3>交易帖</h3>
          {myPosts.length ? myPosts.map((post) => (
            <div className="mineRow" key={post.id}>
              <button type="button" onClick={() => props.onOpen({ type: 'post', item: post })}>{post.title}</button>
              <button type="button" className="editIcon" title="编辑" onClick={() => props.onEdit({ type: 'post', item: post })}><Pencil size={16} /></button>
              <button type="button" className="dangerIcon" onClick={() => remove('posts', post.id)}><Trash2 size={16} /></button>
            </div>
          )) : <p className="emptyState">还没有发布交易帖。</p>}
        </div>
        <div>
          <h3>日常</h3>
          {myMoments.length ? myMoments.map((moment) => (
            <div className="mineRow" key={moment.id}>
              <button type="button" onClick={() => props.onOpen({ type: 'moment', item: moment })}>{moment.petName} 的日常</button>
              <button type="button" className="editIcon" title="编辑" onClick={() => props.onEdit({ type: 'moment', item: moment })}><Pencil size={16} /></button>
              <button type="button" className="dangerIcon" onClick={() => remove('moments', moment.id)}><Trash2 size={16} /></button>
            </div>
          )) : <p className="emptyState">还没有发布日常。</p>}
        </div>
      </div>
    </div>
  );
}

function Composer({ categories, referenceData, currentUser, onSuccess }: { categories: Category[]; referenceData: ReferenceData; currentUser: AppUser | null; onSuccess: () => void }) {
  const [mode, setMode] = React.useState<'post' | 'moment'>('post');
  const [imageUrl, setImageUrl] = React.useState('');
  const [imagePreview, setImagePreview] = React.useState('');
  const [busy, setBusy] = React.useState(false);
  const [error, setError] = React.useState('');
  const regions = referenceData.regions.length ? referenceData.regions : fallbackRegions;
  const [province, setProvince] = React.useState(regions[0].name);
  const selectedProvince = regions.find((item) => item.name === province) || regions[0];
  const [city, setCity] = React.useState(selectedProvince.cities[0].name);
  const selectedCity = selectedProvince.cities.find((item) => item.name === city) || selectedProvince.cities[0];
  const [district, setDistrict] = React.useState(selectedCity.districts[0]);

  React.useEffect(() => {
    const nextCity = selectedProvince.cities[0].name;
    setCity(nextCity);
    setDistrict(selectedProvince.cities[0].districts[0]);
  }, [province]);

  React.useEffect(() => setDistrict(selectedCity.districts[0]), [city]);

  React.useEffect(() => {
    if (!regions.some((item) => item.name === province)) {
      setProvince(regions[0].name);
      setCity(regions[0].cities[0].name);
      setDistrict(regions[0].cities[0].districts[0]);
    }
  }, [regions, province]);

  React.useEffect(() => () => {
    if (imagePreview) URL.revokeObjectURL(imagePreview);
  }, [imagePreview]);

  async function upload(file: File) {
    setError('');
    const validationError = validateImage(file);
    if (validationError) {
      setImageUrl('');
      setImagePreview('');
      return setError(validationError);
    }
    const previewUrl = URL.createObjectURL(file);
    setImagePreview((oldPreview) => {
      if (oldPreview) URL.revokeObjectURL(oldPreview);
      return previewUrl;
    });
    const form = new FormData();
    form.append('file', file);
    try {
      const res = await fetch(`${API_BASE}/upload`, { method: 'POST', body: form });
      if (!res.ok) throw new Error(await readError(res));
      setImageUrl((await res.json()).url);
    } catch (err) {
      setError(err instanceof Error ? err.message : '图片上传失败，请稍后重试。');
    }
  }

  async function submit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError('');
    const formElement = event.currentTarget;
    if (!currentUser) return setError('请先登录后再发布内容。');
    const form = new FormData(formElement);
    const body = Object.fromEntries(form.entries());
    if (phonePattern.test(Object.values(body).join(' '))) return setError('请不要填写手机号，平台只允许站内私信沟通。');
    setBusy(true);
    body.author = currentUser.nickname;
    body.city = `${province} ${city} ${district}`;
    body.contact = CONTACT_VALUE;
    if (imageUrl) body.imageUrl = imageUrl;
    try {
      const res = await fetch(`${API_BASE}/${mode === 'post' ? 'posts' : 'moments'}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
      });
      if (!res.ok) throw new Error(await readError(res));
      formElement.reset();
      setImageUrl('');
      setImagePreview('');
      onSuccess();
    } catch (err) {
      setError(err instanceof Error ? err.message : '发布失败，请检查内容是否符合规则。');
    } finally {
      setBusy(false);
    }
  }

  return (
    <aside className="composer">
      <div className="composerHeader"><h3>发布中心</h3><span>{currentUser ? `当前用户：${currentUser.nickname}` : '登录后可发布'}</span></div>
      <div className="tabs"><button type="button" className={mode === 'post' ? 'active' : ''} onClick={() => setMode('post')}>交易帖</button><button type="button" className={mode === 'moment' ? 'active' : ''} onClick={() => setMode('moment')}>日常</button></div>
      {!currentUser && <div className="locked"><Lock size={18} /><span>请先在右上角登录，之后才能发布帖子或日常。</span></div>}
      <form onSubmit={submit}>
        {mode === 'post' ? (
          <>
            <input name="title" placeholder="标题" required disabled={!currentUser} />
            <select name="type" defaultValue={referenceData.postTypes[0] || '互换'} disabled={!currentUser}>{referenceData.postTypes.map((type) => <option key={type}>{type}</option>)}</select>
          </>
        ) : <input name="petName" placeholder="宠物名字" required disabled={!currentUser} />}
        <select name="category" required disabled={!currentUser}>{categories.map((category) => <option key={category.id} value={category.name}>{category.name}</option>)}</select>
        <RegionPicker province={province} city={city} district={district} selectedProvince={selectedProvince} selectedCity={selectedCity} regions={regions} disabled={!currentUser} onProvince={setProvince} onCity={setCity} onDistrict={setDistrict} />
        {mode === 'post' ? <textarea name="description" placeholder="描述需求、宠物状态或交易条件。请勿填写手机号。" required disabled={!currentUser} /> : <textarea name="content" placeholder="分享今天的宠物日常。请勿填写手机号。" required disabled={!currentUser} />}
        <div className="contactOnly"><MessageCircle size={16} /><span>联系方式固定为站内私信</span></div>
        <label className="fileInput"><Camera size={18} /><span>{imageUrl ? '图片已上传' : '上传图片'}</span><input type="file" accept="image/*" disabled={!currentUser} onChange={(e) => e.target.files?.[0] && upload(e.target.files[0])} /></label>
        <p className="fileHint">支持 JPG、PNG、WebP、GIF，单张不超过 5MB。</p>
        {imagePreview && <div className="imagePreview"><img src={imagePreview} alt="上传预览" /><button type="button" onClick={() => { setImageUrl(''); setImagePreview(''); }}>移除图片</button></div>}
        {error && <p className="formError">{error}</p>}
        <button className="submit" disabled={busy || !currentUser}>{busy ? '发布中...' : '发布'}</button>
      </form>
    </aside>
  );
}

function EditModal(props: {
  detail: { type: 'post' | 'moment'; item: MarketPost | Moment };
  categories: Category[];
  referenceData: ReferenceData;
  currentUser: AppUser | null;
  onClose: () => void;
  onSaved: () => void;
}) {
  const item = props.detail.item;
  const regions = props.referenceData.regions.length ? props.referenceData.regions : fallbackRegions;
  const initialRegion = parseRegion(props.detail.type === 'post' ? (item as MarketPost).city : (item as Moment).city, regions);
  const [province, setProvince] = React.useState(initialRegion.province);
  const selectedProvince = regions.find((region) => region.name === province) || regions[0];
  const [city, setCity] = React.useState(initialRegion.city);
  const selectedCity = selectedProvince.cities.find((regionCity) => regionCity.name === city) || selectedProvince.cities[0];
  const [district, setDistrict] = React.useState(initialRegion.district);
  const [error, setError] = React.useState('');
  const [busy, setBusy] = React.useState(false);

  React.useEffect(() => {
    const nextCity = selectedProvince.cities[0].name;
    setCity(nextCity);
    setDistrict(selectedProvince.cities[0].districts[0]);
  }, [province]);

  React.useEffect(() => setDistrict(selectedCity.districts[0]), [city]);

  async function submit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!props.currentUser) return setError('请先登录后再编辑内容。');
    const form = new FormData(event.currentTarget);
    const body = Object.fromEntries(form.entries());
    if (phonePattern.test(Object.values(body).join(' '))) return setError('请不要填写手机号，平台只允许站内私信沟通。');
    setBusy(true);
    setError('');
    body.author = props.currentUser.nickname;
    body.city = `${province} ${city} ${district}`;
    if (props.detail.type === 'post') {
      body.contact = CONTACT_VALUE;
      body.imageUrl = (item as MarketPost).imageUrl || '';
    } else {
      body.imageUrl = (item as Moment).imageUrl || '';
    }
    try {
      const kind = props.detail.type === 'post' ? 'posts' : 'moments';
      const author = encodeURIComponent(props.currentUser.nickname);
      const res = await fetch(`${API_BASE}/${kind}/${item.id}?author=${author}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
      });
      if (!res.ok) throw new Error(await readError(res));
      props.onSaved();
    } catch (err) {
      setError(err instanceof Error ? err.message : '保存失败，请检查内容后再试。');
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="modalBackdrop" onClick={props.onClose}>
      <article className="detailModal editModal" onClick={(event) => event.stopPropagation()}>
        <button type="button" className="closeButton" onClick={props.onClose}><X size={18} /></button>
        <h2>{props.detail.type === 'post' ? '编辑交易帖' : '编辑日常'}</h2>
        <form onSubmit={submit}>
          {props.detail.type === 'post' ? (
            <>
              <input name="title" defaultValue={(item as MarketPost).title} placeholder="标题" required />
              <select name="type" defaultValue={(item as MarketPost).type}>{props.referenceData.postTypes.map((type) => <option key={type}>{type}</option>)}</select>
            </>
          ) : <input name="petName" defaultValue={(item as Moment).petName} placeholder="宠物名字" required />}
          <select name="category" defaultValue={props.detail.type === 'post' ? (item as MarketPost).category : (item as Moment).category} required>
            {props.categories.map((category) => <option key={category.id} value={category.name}>{category.name}</option>)}
          </select>
          <RegionPicker province={province} city={city} district={district} selectedProvince={selectedProvince} selectedCity={selectedCity} regions={regions} disabled={false} onProvince={setProvince} onCity={setCity} onDistrict={setDistrict} />
          {props.detail.type === 'post'
            ? <textarea name="description" defaultValue={(item as MarketPost).description} placeholder="描述需求、宠物状态或交易条件。请勿填写手机号。" required />
            : <textarea name="content" defaultValue={(item as Moment).content} placeholder="分享今天的宠物日常。请勿填写手机号。" required />}
          <div className="contactOnly"><MessageCircle size={16} /><span>联系方式固定为站内私信</span></div>
          {error && <p className="formError">{error}</p>}
          <button className="submit" disabled={busy}>{busy ? '保存中...' : '保存修改'}</button>
        </form>
      </article>
    </div>
  );
}

function DetailModal({ detail, onClose }: { detail: { type: 'post' | 'moment' | 'pet'; item: MarketPost | Moment | Pet }; onClose: () => void }) {
  const item = detail.item;
  const title = detail.type === 'post' ? (item as MarketPost).title : detail.type === 'moment' ? `${(item as Moment).petName} 的日常` : (item as Pet).name;
  return (
    <div className="modalBackdrop" onClick={onClose}>
      <article className="detailModal" onClick={(event) => event.stopPropagation()}>
        <button type="button" className="closeButton" onClick={onClose}><X size={18} /></button>
        <h2>{title}</h2>
        {detail.type === 'post' && <DetailRows rows={[
          ['类型', (item as MarketPost).type],
          ['分类', (item as MarketPost).category],
          ['地区', (item as MarketPost).city],
          ['发布人', (item as MarketPost).author],
          ['联系', (item as MarketPost).contact || CONTACT_VALUE],
          ['描述', (item as MarketPost).description]
        ]} />}
        {detail.type === 'moment' && <DetailRows rows={[
          ['分类', (item as Moment).category || '日常'],
          ['地区', (item as Moment).city || '未选择地区'],
          ['宠物', (item as Moment).petName],
          ['作者', (item as Moment).author],
          ['点赞', String((item as Moment).likes)],
          ['内容', (item as Moment).content]
        ]} />}
        {detail.type === 'pet' && <DetailRows rows={[
          ['分类', (item as Pet).category],
          ['品种', (item as Pet).breed],
          ['年龄', (item as Pet).age],
          ['地区', (item as Pet).city],
          ['状态', (item as Pet).status],
          ['价格', (item as Pet).price > 0 ? `￥${(item as Pet).price}` : '面议'],
          ['健康', (item as Pet).healthInfo],
          ['性格', (item as Pet).personality]
        ]} />}
      </article>
    </div>
  );
}

function DetailRows({ rows }: { rows: Array<[string, string]> }) {
  return <dl className="detailRows">{rows.map(([key, value]) => <React.Fragment key={key}><dt>{key}</dt><dd>{value}</dd></React.Fragment>)}</dl>;
}

function EmptyState({ title, helper }: { title: string; helper: string }) {
  return <div className="emptyBlock"><PawPrint size={26} /><strong>{title}</strong><span>{helper}</span></div>;
}

function matchesCategory(filter: string, category?: string) {
  return filter === '全部' || category === filter;
}

function matchesCity(filter: string, city?: string) {
  return filter === '全部' || (city || '').includes(filter);
}

function matchesType(filter: string, type?: string) {
  return filter === '全部' || type === filter;
}

function matchesText(query: string, fields: Array<string | undefined>) {
  const keyword = query.trim().toLowerCase();
  if (!keyword) return true;
  return fields.some((field) => (field || '').toLowerCase().includes(keyword));
}

function sortByTime<T extends { createdAt?: string }>(items: T[], mode: 'latest' | 'oldest') {
  return [...items].sort((left, right) => {
    const leftTime = left.createdAt ? new Date(left.createdAt).getTime() : 0;
    const rightTime = right.createdAt ? new Date(right.createdAt).getTime() : 0;
    return mode === 'latest' ? rightTime - leftTime : leftTime - rightTime;
  });
}

function cityOptions(regions: Region[]) {
  return Array.from(new Set(regions.flatMap((province) => province.cities.map((city) => city.name))));
}

function validateImage(file: File) {
  if (!allowedImageTypes.includes(file.type)) {
    return '图片格式不支持，请上传 JPG、PNG、WebP 或 GIF。';
  }
  if (file.size > MAX_IMAGE_SIZE) {
    return '图片不能超过 5MB，请压缩后再上传。';
  }
  return '';
}

function parseRegion(value: string | undefined, regions: Region[]) {
  const [provinceName, cityName, districtName] = (value || '').split(/\s+/);
  const province = regions.find((item) => item.name === provinceName) || regions[0];
  const city = province.cities.find((item) => item.name === cityName) || province.cities[0];
  const district = city.districts.includes(districtName) ? districtName : city.districts[0];
  return { province: province.name, city: city.name, district };
}

async function readError(res: Response) {
  try {
    const data = await res.json();
    return data.message || data.error || '请求失败，请检查填写内容。';
  } catch {
    return '请求失败，请检查后端服务是否正常运行。';
  }
}

function RegionPicker(props: {
  province: string; city: string; district: string; selectedProvince: Region; selectedCity: Region['cities'][number]; regions: Region[]; disabled: boolean; onProvince: (value: string) => void; onCity: (value: string) => void; onDistrict: (value: string) => void;
}) {
  return (
    <div className="regionGrid">
      <select value={props.province} disabled={props.disabled} onChange={(event) => props.onProvince(event.target.value)}>{props.regions.map((item) => <option key={item.name}>{item.name}</option>)}</select>
      <select value={props.city} disabled={props.disabled} onChange={(event) => props.onCity(event.target.value)}>{props.selectedProvince.cities.map((item) => <option key={item.name}>{item.name}</option>)}</select>
      <select value={props.district} disabled={props.disabled} onChange={(event) => props.onDistrict(event.target.value)}>{props.selectedCity.districts.map((item) => <option key={item}>{item}</option>)}</select>
    </div>
  );
}

function Metric({ value, label }: { value: number; label: string }) {
  return <div className="metric"><strong>{value}</strong><span>{label}</span></div>;
}

function SectionTitle({ icon, title, helper }: { icon: React.ReactNode; title: string; helper: string }) {
  return <div className="sectionTitle"><div>{icon}</div><div><h2>{title}</h2><p>{helper}</p></div></div>;
}

function placeholder(label: string) {
  return <div className="placeholder"><PawPrint size={34} /><span>{label}</span></div>;
}

function imageBox(url: string, label: string) {
  return url ? <img className="cover" src={url} alt={label} /> : placeholder(label);
}

ReactDOM.createRoot(document.getElementById('root')!).render(<App />);
