import { HttpErrorResponse } from '@angular/common/http';

/** Normalized RFC 9457 problem detail returned by the backend. */
export interface ApiError {
  status: number;
  title: string;
  detail: string;
  code?: string;
  errors?: Record<string, string>;
}

export function toApiError(error: unknown): ApiError {
  if (error instanceof HttpErrorResponse) {
    if (error.status === 0) {
      return { status: 0, title: 'Sin conexión', detail: 'No se pudo conectar con el servidor.' };
    }
    const body = (error.error ?? {}) as Partial<ApiError>;
    return {
      status: error.status,
      title: body.title ?? error.statusText,
      detail: body.detail ?? 'Ocurrió un error inesperado.',
      code: body.code,
      errors: body.errors,
    };
  }
  if (isApiError(error)) {
    return error;
  }
  return { status: -1, title: 'Error', detail: 'Ocurrió un error inesperado.' };
}

export function isApiError(value: unknown): value is ApiError {
  return typeof value === 'object' && value !== null && 'status' in value && 'detail' in value;
}

/** Messages in Spanish for the business codes the backend emits. */
const CODE_MESSAGES: Record<string, string> = {
  INVALID_CONTRIBUTION_AMOUNT: 'El monto del aporte debe ser mayor a cero.',
  DEADLINE_PASSED: 'La campaña ya alcanzó su fecha límite y no acepta aportes.',
  CAMPAIGN_NOT_ACCEPTING_CONTRIBUTIONS: 'La campaña no está aceptando aportes.',
  CAMPAIGN_HAS_CONTRIBUTIONS: 'Una campaña con aportes no puede cancelarse; solo puede cerrarse.',
  INVALID_CAMPAIGN: 'Los datos de la campaña no son válidos.',
  INVALID_CAMPAIGN_STATE: 'La campaña no permite esta acción en su estado actual.',
  FORBIDDEN: 'No cuenta con permisos para realizar esta acción.',
  CONFLICT: 'La operación entró en conflicto con otra. Intente nuevamente.',
  NOT_FOUND: 'El recurso solicitado no existe.',
  VALIDATION_FAILED: 'Revise los datos del formulario.',
};

export function friendlyMessage(error: ApiError): string {
  if (error.code === 'FORBIDDEN' && error.detail) {
    return error.detail.includes('private')
      ? 'El creador de esta campaña decidió mantener privados a sus aportantes.'
      : CODE_MESSAGES['FORBIDDEN'];
  }
  return (error.code && CODE_MESSAGES[error.code]) || error.detail;
}
