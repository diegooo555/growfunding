import { Pipe, PipeTransform } from '@angular/core';

const HOUR = 3_600_000;
const DAY = 24 * HOUR;

export function describeTimeLeft(deadline: string | Date, now: Date = new Date()): string {
  const diff = new Date(deadline).getTime() - now.getTime();
  if (diff <= 0) {
    return 'Finalizada';
  }
  if (diff < HOUR) {
    const minutes = Math.max(1, Math.floor(diff / 60_000));
    return `Quedan ${minutes} min`;
  }
  if (diff < DAY) {
    const hours = Math.floor(diff / HOUR);
    return `Quedan ${hours} h`;
  }
  const days = Math.floor(diff / DAY);
  return days === 1 ? 'Queda 1 día' : `Quedan ${days} días`;
}

@Pipe({ name: 'daysLeft' })
export class DaysLeftPipe implements PipeTransform {
  transform(deadline: string | Date): string {
    return describeTimeLeft(deadline);
  }
}
