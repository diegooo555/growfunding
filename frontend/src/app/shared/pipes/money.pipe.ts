import { Pipe, PipeTransform } from '@angular/core';

const formatters = new Map<string, Intl.NumberFormat>();

export function formatMoney(amount: number | null | undefined, currency = 'USD'): string {
  if (amount === null || amount === undefined || Number.isNaN(Number(amount))) {
    return '—';
  }
  let formatter = formatters.get(currency);
  if (!formatter) {
    formatter = new Intl.NumberFormat('es', {
      style: 'currency',
      currency,
      minimumFractionDigits: 0,
      maximumFractionDigits: 2,
    });
    formatters.set(currency, formatter);
  }
  return formatter.format(Number(amount));
}

@Pipe({ name: 'money' })
export class MoneyPipe implements PipeTransform {
  transform(amount: number | null | undefined, currency = 'USD'): string {
    return formatMoney(amount, currency);
  }
}
