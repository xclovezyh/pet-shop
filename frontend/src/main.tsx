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
  Plus,
  Search,
  ShieldCheck,
  Store,
  Tags,
  User
} from 'lucide-react';
import './styles.css';

const API_BASE = '/api';
const CONTACT_VALUE = '站内私信';
const phonePattern = /(?:\+?86[-\s]?)?1[3-9]\d{9}/;

type AppUser = {
  id: number;
  nickname: string;
};

type Category = {
  id: number;
  name: string;
  description: string;
  tags: string;
};

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
};

type Moment = {
  id: number;
  author: string;
  petName: string;
  category?: string;
  content: string;
  likes: number;
  imageUrl: string;
};

type Region = {
  name: string;
  cities: Array<{
    name: string;
    districts: string[];
  }>;
};

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
  {
    name: '浙江省',
    cities: [
      { name: '杭州市', districts: ['西湖区', '拱墅区', '滨江区', '余杭区'] },
      { name: '宁波市', districts: ['海曙区', '鄞州区', '江北区'] }
    ]
  },
  {
    name: '江苏省',
    cities: [
      { name: '南京市', districts: ['玄武区', '秦淮区', '建邺区'] },
      { name: '苏州市', districts: ['姑苏区', '吴中区', '工业园区'] }
    ]
  },
  {
    name: '广东省',
    cities: [
      { name: '广州市', districts: ['天河区', '越秀区', '番禺区'] },
      { name: '深圳市', districts: ['南山区', '福田区', '宝安区'] }
    ]
  }
];

const fallbackReferenceData: ReferenceData = {
  regions: fallbackRegions,
  postTypes: ['互换', '售卖', '领养', '闲置', '求助', '寄养', '寻宠', '相亲配种'],
  petStatuses: ['在售', '可领养', '可互换', '已预订', '已成交', '暂不开放'],
  petGenders: ['公', '母', '未知'],
  ageRanges: ['幼年', '青年', '成年', '老年'],
  healthRecords: ['疫苗齐全', '已驱虫', '已绝育', '体检正常', '需复查', '特殊护理'],
  personalityTags: ['亲人', '安静', '活泼', '胆小', '独立', '粘人', '适合新手', '适合有经验家庭'],
  serviceTags: ['站内私信', '同城自提', '线下看宠', '寄养互助', '闲置转让', '领养审核']
};

const demoCategories: Category[] = [
  { id: 1, name: '猫咪', description: '温顺亲人，适合公寓和家庭陪伴。', tags: '新手友好,安静,陪伴型' },
  { id: 2, name: '狗狗', description: '活泼忠诚，需要规律运动和训练。', tags: '互动强,需要遛弯,家庭型' },
  { id: 3, name: '小宠', description: '仓鼠、兔子、龙猫等，占地小但需要细心照顾。', tags: '空间小,易观察,轻陪伴' },
  { id: 4, name: '水族', description: '观赏性强，适合打造安静的家居角落。', tags: '观赏型,低噪音,设备需求' },
  { id: 5, name: '鸟类', description: '鹦鹉、文鸟、金丝雀等，需要稳定笼舍和互动训练。', tags: '鸣叫,训练,环境敏感' },
  { id: 6, name: '爬宠', description: '龟、守宫、蜥蜴、蛇等，重点关注温湿度和饲养箱。', tags: '温控,进阶饲养,低互动' },
  { id: 7, name: '异宠', description: '蜜袋鼯、刺猬等特殊宠物，适合有经验的饲养者。', tags: '特殊护理,经验要求,夜行' },
  { id: 8, name: '用品', description: '食品、玩具、猫爬架、牵引绳等宠物用品。', tags: '闲置交易,日常消耗,养宠装备' }
];

const demoPets: Pet[] = [
  { id: 1, name: '团子', category: '猫咪', breed: '英短银渐层', age: '8个月', city: '上海市 上海市 浦东新区', status: '在售', price: 1800, imageUrl: '', healthInfo: '疫苗齐全，已驱虫', personality: '安静亲人，喜欢陪睡' },
  { id: 2, name: '可乐', category: '狗狗', breed: '柯基', age: '1岁', city: '浙江省 杭州市 西湖区', status: '可互换', price: 0, imageUrl: '', healthInfo: '体检正常，精力充沛', personality: '活泼黏人，会坐下握手' },
  { id: 3, name: '雪球', category: '小宠', breed: '侏儒兔', age: '5个月', city: '江苏省 南京市 玄武区', status: '可领养', price: 0, imageUrl: '', healthInfo: '健康，饮食稳定', personality: '胆小但熟悉后很亲近' }
];

const demoPosts: MarketPost[] = [
  { id: 1, title: '想给柯基找同城互换寄养伙伴', type: '互换', category: '狗狗', city: '浙江省 杭州市 西湖区', description: '工作日偶尔出差，希望找同城稳定互助家庭。', author: '林小满', contact: CONTACT_VALUE, imageUrl: '' },
  { id: 2, title: '英短银渐层找新家', type: '售卖', category: '猫咪', city: '上海市 上海市 浦东新区', description: '自家猫宝宝，疫苗驱虫记录完整，可预约看猫。', author: '阿舟', contact: CONTACT_VALUE, imageUrl: '' },
  { id: 3, title: '闲置猫爬架转让', type: '闲置', category: '用品', city: '江苏省 苏州市 姑苏区', description: '九成新，适合小户型，支持站内私信沟通。', author: '南栀', contact: CONTACT_VALUE, imageUrl: '' }
];

const demoMoments: Moment[] = [
  { id: 1, author: '林小满', petName: '团子', category: '猫咪', content: '今天第一次学会自己开零食罐，已经开始怀疑家里的安全系统。', likes: 28, imageUrl: '' },
  { id: 2, author: '阿舟', petName: '可乐', category: '狗狗', content: '雨停后去公园跑了两圈，回来直接睡成一张毯子。', likes: 16, imageUrl: '' }
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
  const [currentUser, setCurrentUser] = React.useState<AppUser | null>(() => {
    const raw = localStorage.getItem('petshop_user');
    return raw ? JSON.parse(raw) : null;
  });

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
        <div className="brand">
          <PawPrint />
          <span>萌宠集市</span>
        </div>
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
          <p className="lead">发布前需要登录；所有交易沟通仅支持站内私信，页面和后端都会拦截手机号。</p>
          <div className="search">
            <Search size={20} />
            <input placeholder="搜索猫咪、柯基、互换、领养、闲置用品" />
          </div>
        </div>
        <div className="heroPanel">
          <Metric value={categories.data.length} label="分类库" />
          <Metric value={pets.data.length} label="展示宠物" />
          <Metric value={posts.data.length} label="交易帖子" />
        </div>
      </section>

      <section id="categories" className="section">
        <SectionTitle icon={<Tags />} title="宠物分类库" helper="发布内容时必须从这里选择分类" />
        <div className="categoryGrid">
          {categories.loading && <p>正在加载分类...</p>}
          {categories.data.map((category) => (
            <article className="category" key={category.id}>
              <h3>{category.name}</h3>
              <p>{category.description}</p>
              <div className="chips">
                {category.tags.split(',').map((tag) => <span key={tag}>{tag}</span>)}
              </div>
            </article>
          ))}
        </div>
      </section>

      <section id="pets" className="section">
        <SectionTitle icon={<Store />} title="宠物展示与售卖" helper="先展示基本信息，后续可扩展订单和审核" />
        <div className="petGrid">
          {pets.data.map((pet) => (
            <article className="petCard" key={pet.id}>
              {imageBox(pet.imageUrl, pet.name)}
              <div className="petInfo">
                <div className="between">
                  <h3>{pet.name}</h3>
                  <span className="status">{pet.status}</span>
                </div>
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
        </div>
      </section>

      <section id="market" className="section split">
        <div>
          <SectionTitle icon={<Plus />} title="售卖 / 互换 / 领养帖子" helper="所有联系入口统一为站内私信" />
          <PostList posts={posts.data} />
        </div>
        <Composer categories={categories.data} referenceData={referenceData.data} currentUser={currentUser} onSuccess={reloadFeeds} />
      </section>

      <section id="moments" className="section">
        <SectionTitle icon={<Camera />} title="用户日常分享" helper="记录宠物近况、养护经验和可爱的日常瞬间" />
        <MomentList moments={moments.data} />
      </section>

      <section id="mine" className="section">
        <SectionTitle icon={<User />} title="我的发布" helper="登录后可集中查看自己发布的交易帖和日常" />
        <MyPanel currentUser={currentUser} posts={posts.data} moments={moments.data} />
      </section>
    </main>
  );
}

function LoginBox({ currentUser, onLogin, onLogout }: { currentUser: AppUser | null; onLogin: (user: AppUser) => void; onLogout: () => void }) {
  const [name, setName] = React.useState('');
  const [error, setError] = React.useState('');
  const [busy, setBusy] = React.useState(false);

  if (currentUser) {
    return (
      <div className="userBadge">
        <User size={16} />
        <span>{currentUser.nickname}</span>
        <button type="button" onClick={onLogout}>退出</button>
      </div>
    );
  }

  async function submit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const nickname = name.trim();
    setError('');
    if (!nickname) {
      setError('请输入昵称');
      return;
    }
    if (phonePattern.test(nickname)) {
      setError('昵称不能使用手机号');
      return;
    }
    setBusy(true);
    try {
      const res = await fetch(`${API_BASE}/users/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ nickname })
      });
      if (!res.ok) {
        throw new Error(await readError(res));
      }
      const user = await res.json();
      onLogin(user);
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

function PostList({ posts }: { posts: MarketPost[] }) {
  return (
    <div className="postList">
      {posts.map((post) => (
        <article className="post" key={post.id}>
          <div className="between">
            <span className="type">{post.type}</span>
            <span className="postContact"><MessageCircle size={15} />{post.contact || CONTACT_VALUE}</span>
          </div>
          <h3>{post.title}</h3>
          <p>{post.description}</p>
          <div className="postMeta">
            <span>{post.category}</span>
            <span>{post.city}</span>
            <span>{post.author}</span>
          </div>
        </article>
      ))}
    </div>
  );
}

function MomentList({ moments }: { moments: Moment[] }) {
  return (
    <div className="momentGrid">
      {moments.map((moment) => (
        <article className="moment" key={moment.id}>
          {imageBox(moment.imageUrl, moment.petName || moment.author)}
          <div>
            <span className="type">{moment.category || '日常'}</span>
            <h3>{moment.author} 和 {moment.petName}</h3>
            <p>{moment.content}</p>
            <span className="likes"><Heart size={15} />{moment.likes}</span>
          </div>
        </article>
      ))}
    </div>
  );
}

function MyPanel({ currentUser, posts, moments }: { currentUser: AppUser | null; posts: MarketPost[]; moments: Moment[] }) {
  if (!currentUser) {
    return <div className="myPanel emptyState">登录后，这里会显示你发布的交易帖和日常。</div>;
  }
  const myPosts = posts.filter((post) => post.author === currentUser.nickname);
  const myMoments = moments.filter((moment) => moment.author === currentUser.nickname);

  return (
    <div className="myPanel">
      <div className="mySummary">
        <div><strong>{myPosts.length}</strong><span>我的帖子</span></div>
        <div><strong>{myMoments.length}</strong><span>我的日常</span></div>
      </div>
      <div className="myColumns">
        <div>
          <h3>交易帖</h3>
          {myPosts.length ? <PostList posts={myPosts} /> : <p className="emptyState">还没有发布交易帖。</p>}
        </div>
        <div>
          <h3>日常</h3>
          {myMoments.length ? <MomentList moments={myMoments} /> : <p className="emptyState">还没有发布日常。</p>}
        </div>
      </div>
    </div>
  );
}

function Composer({ categories, referenceData, currentUser, onSuccess }: { categories: Category[]; referenceData: ReferenceData; currentUser: AppUser | null; onSuccess: () => void }) {
  const [mode, setMode] = React.useState<'post' | 'moment'>('post');
  const [imageUrl, setImageUrl] = React.useState('');
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

  React.useEffect(() => {
    setDistrict(selectedCity.districts[0]);
  }, [city]);

  React.useEffect(() => {
    if (!regions.some((item) => item.name === province)) {
      const nextProvince = regions[0];
      setProvince(nextProvince.name);
      setCity(nextProvince.cities[0].name);
      setDistrict(nextProvince.cities[0].districts[0]);
    }
  }, [regions, province]);

  async function upload(file: File) {
    setError('');
    const form = new FormData();
    form.append('file', file);
    try {
      const res = await fetch(`${API_BASE}/upload`, { method: 'POST', body: form });
      if (!res.ok) {
        throw new Error(await readError(res));
      }
      const data = await res.json();
      setImageUrl(data.url);
    } catch (err) {
      setError(err instanceof Error ? err.message : '图片上传失败，请稍后重试。');
    }
  }

  async function submit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError('');
    const formElement = event.currentTarget;
    if (!currentUser) {
      setError('请先登录后再发布内容。');
      return;
    }

    const form = new FormData(formElement);
    const body = Object.fromEntries(form.entries());
    const contentToCheck = Object.values(body).join(' ');
    if (phonePattern.test(contentToCheck)) {
      setError('请不要填写手机号，平台只允许站内私信沟通。');
      return;
    }

    setBusy(true);
    body.author = currentUser.nickname;
    body.city = `${province} ${city} ${district}`;
    body.contact = CONTACT_VALUE;
    if (imageUrl) {
      body.imageUrl = imageUrl;
    }

    try {
      const res = await fetch(`${API_BASE}/${mode === 'post' ? 'posts' : 'moments'}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
      });
      if (!res.ok) {
        throw new Error(await readError(res));
      }
      formElement.reset();
      setImageUrl('');
      onSuccess();
    } catch (err) {
      setError(err instanceof Error ? err.message : '发布失败，请检查登录状态、分类、地区和内容是否符合规则。');
    } finally {
      setBusy(false);
    }
  }

  return (
    <aside className="composer">
      <div className="composerHeader">
        <h3>发布中心</h3>
        <span>{currentUser ? `当前用户：${currentUser.nickname}` : '登录后可发布'}</span>
      </div>
      <div className="tabs">
        <button type="button" className={mode === 'post' ? 'active' : ''} onClick={() => setMode('post')}>交易帖</button>
        <button type="button" className={mode === 'moment' ? 'active' : ''} onClick={() => setMode('moment')}>日常</button>
      </div>
      {!currentUser && (
        <div className="locked">
          <Lock size={18} />
          <span>请先在右上角登录，之后才能发布帖子或日常。</span>
        </div>
      )}
      <form onSubmit={submit}>
        {mode === 'post' ? (
          <>
            <input name="title" placeholder="标题" required disabled={!currentUser} />
            <select name="type" defaultValue={referenceData.postTypes[0] || '互换'} disabled={!currentUser}>
              {referenceData.postTypes.map((type) => <option key={type}>{type}</option>)}
            </select>
          </>
        ) : (
          <input name="petName" placeholder="宠物名字" required disabled={!currentUser} />
        )}
        <select name="category" required disabled={!currentUser}>
          {categories.map((category) => <option key={category.id} value={category.name}>{category.name}</option>)}
        </select>
        <RegionPicker
          province={province}
          city={city}
          district={district}
          selectedProvince={selectedProvince}
          selectedCity={selectedCity}
          regions={regions}
          disabled={!currentUser}
          onProvince={setProvince}
          onCity={setCity}
          onDistrict={setDistrict}
        />
        {mode === 'post' ? (
          <textarea name="description" placeholder="描述需求、宠物状态或交易条件。请勿填写手机号。" required disabled={!currentUser} />
        ) : (
          <textarea name="content" placeholder="分享今天的宠物日常。请勿填写手机号。" required disabled={!currentUser} />
        )}
        <div className="contactOnly">
          <MessageCircle size={16} />
          <span>联系方式固定为站内私信</span>
        </div>
        <label className="fileInput">
          <Camera size={18} />
          <span>{imageUrl ? '图片已上传' : '上传图片'}</span>
          <input type="file" accept="image/*" disabled={!currentUser} onChange={(e) => e.target.files?.[0] && upload(e.target.files[0])} />
        </label>
        {error && <p className="formError">{error}</p>}
        <button className="submit" disabled={busy || !currentUser}>{busy ? '发布中...' : '发布'}</button>
      </form>
    </aside>
  );
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
  province: string;
  city: string;
  district: string;
  selectedProvince: Region;
  selectedCity: Region['cities'][number];
  regions: Region[];
  disabled: boolean;
  onProvince: (value: string) => void;
  onCity: (value: string) => void;
  onDistrict: (value: string) => void;
}) {
  return (
    <div className="regionGrid">
      <select value={props.province} disabled={props.disabled} onChange={(event) => props.onProvince(event.target.value)}>
        {props.regions.map((item) => <option key={item.name}>{item.name}</option>)}
      </select>
      <select value={props.city} disabled={props.disabled} onChange={(event) => props.onCity(event.target.value)}>
        {props.selectedProvince.cities.map((item) => <option key={item.name}>{item.name}</option>)}
      </select>
      <select value={props.district} disabled={props.disabled} onChange={(event) => props.onDistrict(event.target.value)}>
        {props.selectedCity.districts.map((item) => <option key={item}>{item}</option>)}
      </select>
    </div>
  );
}

function Metric({ value, label }: { value: number; label: string }) {
  return (
    <div className="metric">
      <strong>{value}</strong>
      <span>{label}</span>
    </div>
  );
}

function SectionTitle({ icon, title, helper }: { icon: React.ReactNode; title: string; helper: string }) {
  return (
    <div className="sectionTitle">
      <div>{icon}</div>
      <div>
        <h2>{title}</h2>
        <p>{helper}</p>
      </div>
    </div>
  );
}

function placeholder(label: string) {
  return (
    <div className="placeholder">
      <PawPrint size={34} />
      <span>{label}</span>
    </div>
  );
}

function imageBox(url: string, label: string) {
  if (!url) {
    return placeholder(label);
  }
  return <img className="cover" src={url} alt={label} />;
}

ReactDOM.createRoot(document.getElementById('root')!).render(<App />);
