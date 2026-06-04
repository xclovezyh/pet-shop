export type CityFilterOption = { code: string; name: string };

export function matchesCity(filter: string, cityCode: string | undefined, city: string | undefined, options: CityFilterOption[]) {
  if (!filter) return true;
  if (cityCode) return cityCode === filter;
  const selected = options.find((option) => option.code === filter);
  return Boolean(selected?.name && (city || '').includes(selected.name));
}
