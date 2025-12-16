import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { of } from 'rxjs';

// Interface para resposta da API (usa houseNumber)
export interface AddressResponse {
  addressId?: number;
  userId: number;
  street: string;
  houseNumber: string; // API retorna houseNumber
  complement?: string;
  district: string;
  city: string;
  state: string;
  zipCode: string;
  phoneNumber?: string;
  country?: string;
  createdAt?: string;
}

// Interface para uso interno (usa number)
export interface Address {
  addressId?: number;
  userId: number;
  street: string;
  number: string; // Usado internamente
  complement?: string;
  district: string;
  city: string;
  state: string;
  zipCode: string;
}

// Interface para request da API (usa houseNumber ao invés de number)
export interface AddressRequest {
  userId: number;
  street: string;
  houseNumber: string; // API espera houseNumber
  complement?: string;
  district: string;
  city: string;
  state: string;
  zipCode: string;
  phoneNumber?: string; // Opcional, pode ser enviado
}

export interface AddressLookup {
  zipCode: string;
  street: string;
  district: string;
  city: string;
  state: string;
}

export interface State {
  code: string;
  name: string;
}

export interface City {
  name: string;
}

@Injectable({ providedIn: 'root' })
export class AddressService {
  private base = 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  /**
   * Busca endereço por CEP (ViaCEP)
   */
  lookupByZipCode(zipCode: string): Observable<AddressLookup> {
    // Remove formatação do CEP se houver
    const cleanZipCode = zipCode.replace(/[.-]/g, '');
    return this.http.get<AddressLookup>(
      `${this.base}/api/form/address/lookup`,
      { params: new HttpParams().set('zipCode', cleanZipCode) }
    );
  }

  /**
   * Lista todos os estados brasileiros
   */
  getStates(): Observable<State[]> {
    return this.http.get<State[]>(`${this.base}/api/form/address/states`);
  }

  /**
   * Lista cidades de um estado
   */
  getCitiesByState(state: string): Observable<City[]> {
    return this.http.get<City[]>(
      `${this.base}/api/form/address/cities`,
      { params: new HttpParams().set('state', state) }
    );
  }

  /**
   * Busca endereços de um usuário específico
   * Mapeia houseNumber (da API) para number (uso interno)
   * Tenta primeiro /api/addresses?userId={userId}, se não existir, retorna array vazio
   */
  getAddressesByUserId(userId: number): Observable<Address[]> {
    return this.http.get<AddressResponse[]>(`${this.base}/api/addresses?userId=${userId}`).pipe(
      map((addresses) => 
        addresses.map(addr => ({
          addressId: addr.addressId,
          userId: addr.userId,
          street: addr.street,
          number: addr.houseNumber, // Mapeia houseNumber para number
          complement: addr.complement,
          district: addr.district,
          city: addr.city,
          state: addr.state,
          zipCode: addr.zipCode
        }))
      ),
      // Se a rota não existir ou houver erro, retorna array vazio
      catchError((error) => {
        console.warn('Rota /api/addresses?userId não encontrada, tentando rota alternativa...', error);
        // Tenta rota alternativa se a primeira falhar
        return this.http.get<AddressResponse[]>(`${this.base}/api/form/address/user/${userId}`).pipe(
          map((addresses) => 
            addresses.map(addr => ({
              addressId: addr.addressId,
              userId: addr.userId,
              street: addr.street,
              number: addr.houseNumber,
              complement: addr.complement,
              district: addr.district,
              city: addr.city,
              state: addr.state,
              zipCode: addr.zipCode
            }))
          ),
          catchError(() => {
            // Se ambas as rotas falharem, retorna array vazio
            console.warn('Nenhuma rota de endereço encontrada, retornando array vazio');
            return of([]);
          })
        );
      })
    );
  }

  /**
   * Busca um endereço por ID
   * Mapeia houseNumber (da API) para number (uso interno)
   */
  getAddressById(addressId: number): Observable<Address> {
    return this.http.get<AddressResponse>(`${this.base}/api/addresses/${addressId}`).pipe(
      map((addr) => ({
        addressId: addr.addressId,
        userId: addr.userId,
        street: addr.street,
        number: addr.houseNumber, // Mapeia houseNumber para number
        complement: addr.complement,
        district: addr.district,
        city: addr.city,
        state: addr.state,
        zipCode: addr.zipCode
      }))
    );
  }

  /**
   * Cria um novo endereço
   * Converte Address (com number) para AddressRequest (com houseNumber)
   * Mapeia a resposta (houseNumber) de volta para number
   */
  createAddress(address: Address): Observable<Address> {
    // Mapeia number para houseNumber conforme esperado pela API
    const request: AddressRequest = {
      userId: address.userId,
      street: address.street,
      houseNumber: address.number, // Mapeia number para houseNumber
      complement: address.complement,
      district: address.district,
      city: address.city,
      state: address.state,
      zipCode: address.zipCode
    };
    
    return this.http.post<AddressResponse>(`${this.base}/api/form/address`, request).pipe(
      map((response) => ({
        addressId: response.addressId,
        userId: response.userId,
        street: response.street,
        number: response.houseNumber, // Mapeia houseNumber de volta para number
        complement: response.complement,
        district: response.district,
        city: response.city,
        state: response.state,
        zipCode: response.zipCode
      }))
    );
  }

  /**
   * Atualiza um endereço existente
   * Mapeia a resposta (houseNumber) de volta para number
   */
  updateAddress(addressId: number, address: Address): Observable<Address> {
    // Mapeia number para houseNumber conforme esperado pela API
    const request: AddressRequest = {
      userId: address.userId,
      street: address.street,
      houseNumber: address.number,
      complement: address.complement,
      district: address.district,
      city: address.city,
      state: address.state,
      zipCode: address.zipCode
    };
    
    return this.http.put<AddressResponse>(`${this.base}/api/addresses/${addressId}`, request).pipe(
      map((response) => ({
        addressId: response.addressId,
        userId: response.userId,
        street: response.street,
        number: response.houseNumber, // Mapeia houseNumber de volta para number
        complement: response.complement,
        district: response.district,
        city: response.city,
        state: response.state,
        zipCode: response.zipCode
      }))
    );
  }
}

